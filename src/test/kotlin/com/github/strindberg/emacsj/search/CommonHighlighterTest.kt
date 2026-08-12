package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.testFramework.PlatformTestUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val SCAN_STARTUP_MILLIS = 20L

private const val IDLE_TIMEOUT_SECONDS = 20

class CommonHighlighterTest : EmacsJTestCase() {

    private var savedDelay = HIGHLIGHT_DELAY_MILLIS

    @BeforeEach
    fun speedUpHighlighting() {
        savedDelay = CommonHighlighter.instance.delayMillis
        CommonHighlighter.instance.delayMillis = 0
    }

    @AfterEach
    fun restoreHighlightingDelay() {
        CommonHighlighter.instance.delayMillis = savedDelay
    }

    @Test
    fun `Canceling a search stops the scan rather than letting it run to the end`() {
        // Large enough that one scan takes appreciably longer than anything else in the test.
        myFixture.configureByText("big.txt", "foo bar baz qux ".repeat(60_000))
        val document = myFixture.editor.document

        val fullScan = time {
            CommonHighlighter.instance.findAllAndHighlight(request())
            waitUntilIdle()
        }

        // The scan holds a read action for as long as it runs, so how long the editor waits for the write lock is
        // what says whether canceling actually stopped it.
        CommonHighlighter.instance.findAllAndHighlight(request())
        Thread.sleep(SCAN_STARTUP_MILLIS)
        CommonHighlighter.instance.cancelPending()

        val waitedForLock = time {
            WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "x") }
        }

        assertTrue(
            waitedForLock < fullScan / 2,
            "editor waited ${waitedForLock}ms after canceling, against a full scan of ${fullScan}ms"
        )
    }

    @Test
    fun `A superseded search paints nothing`() {
        myFixture.configureByText("stale.txt", "foo bar ".repeat(2_000))

        // Both issued from the same EDT event, so the first cannot have reached the EDT to paint before the second
        // supersedes it. Painting lives inside the coroutine, so cancelling the job cancels the painting too.
        CommonHighlighter.instance.findAllAndHighlight(request("foo", highlight = true))
        CommonHighlighter.instance.findAllAndHighlight(request("bar", highlight = true))
        waitUntilIdle()
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

        assertEquals(2_000, secondaryHighlightCount())
    }

    private fun secondaryHighlightCount(): Int =
        myFixture.editor.markupModel.allHighlighters.count { it.textAttributesKey == EMACSJ_SECONDARY }

    private fun request(searchArg: String = "foo", highlight: Boolean = false) =
        SearchRequest(
            editor = myFixture.editor,
            project = project,
            searchArg = searchArg,
            useRegexp = false,
            useCase = false,
            highlight = highlight
        )

    /** Pumps while waiting: with painting inside the coroutine, finishing needs the EDT to be free. */
    private fun waitUntilIdle() {
        PlatformTestUtil.waitWithEventsDispatching(
            "search did not finish",
            { CommonHighlighter.instance.isIdle },
            IDLE_TIMEOUT_SECONDS
        )
    }

    private fun time(action: () -> Unit): Long {
        val start = System.nanoTime()
        action()
        return (System.nanoTime() - start) / 1_000_000
    }
}
