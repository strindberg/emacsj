package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.command.WriteCommandAction

private const val SCAN_STARTUP_MILLIS = 20L

class CommonHighlighterTest : EmacsJTestCase() {

    private var savedDelay = HIGHLIGHT_DELAY_MILLIS

    override fun setUp() {
        super.setUp()
        savedDelay = CommonHighlighter.delayMillis
        CommonHighlighter.delayMillis = 0
    }

    override fun tearDown() {
        try {
            CommonHighlighter.delayMillis = savedDelay
        } finally {
            super.tearDown()
        }
    }

    fun `test Canceling a search stops the scan rather than letting it run to the end`() {
        // Large enough that one scan takes appreciably longer than anything else in the test.
        myFixture.configureByText("big.txt", "foo bar baz qux ".repeat(60_000))
        val document = myFixture.editor.document

        val fullScan = time {
            CommonHighlighter.findAllAndHighlight(request())
            waitUntilIdle()
        }

        // The scan holds a read action for as long as it runs, so how long the editor waits for the write lock is
        // what says whether canceling actually stopped it.
        CommonHighlighter.findAllAndHighlight(request())
        Thread.sleep(SCAN_STARTUP_MILLIS)
        CommonHighlighter.cancelPending()

        val waitedForLock = time {
            WriteCommandAction.runWriteCommandAction(project) { document.insertString(0, "x") }
        }

        assertTrue(
            "editor waited ${waitedForLock}ms after canceling, against a full scan of ${fullScan}ms",
            waitedForLock < fullScan / 2
        )
    }

    private fun request() =
        SearchRequest(
            editor = myFixture.editor,
            project = project,
            searchArg = "foo",
            useRegexp = false,
            useCase = false,
            highlight = false
        )

    private fun waitUntilIdle() {
        val deadline = System.currentTimeMillis() + 20_000
        while (!CommonHighlighter.isIdle && System.currentTimeMillis() < deadline) {
            Thread.sleep(1)
        }
        assertTrue("search did not finish", CommonHighlighter.isIdle)
    }

    private fun time(action: () -> Unit): Long {
        val start = System.nanoTime()
        action()
        return (System.nanoTime() - start) / 1_000_000
    }
}
