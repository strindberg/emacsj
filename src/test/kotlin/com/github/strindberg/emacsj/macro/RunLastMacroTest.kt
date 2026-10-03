package com.github.strindberg.emacsj.macro

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.universal.ACTION_CANCEL_REPEAT
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.PlatformTestUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FILE = "macrofile.txt"

private const val PLAYBACK_LAST_MACRO = "PlaybackLastMacro"

private const val TIMEOUT_SECONDS = 10

/** Long enough for the handler's playback poll to have fired several times over. */
private const val SETTLE_MILLIS = 200L

class RunLastMacroTest : EmacsJTestCase() {

    private lateinit var originalPlayback: AnAction

    private lateinit var playback: FakePlayback

    @BeforeEach
    fun rememberPlayback() {
        originalPlayback = ActionManager.getInstance().getAction(PLAYBACK_LAST_MACRO)
    }

    @AfterEach
    fun restorePlayback() {
        ActionManager.getInstance().replaceAction(PLAYBACK_LAST_MACRO, originalPlayback)
    }

    @Test
    fun `Run last macro is disabled when no macro has been recorded`() {
        myFixture.configureByText(FILE, "<caret>foo")

        val presentation = myFixture.testAction(ActionManager.getInstance().getAction(ACTION_RUN_LAST_MACRO))

        assertFalse(presentation.isEnabled)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Run last macro is enabled when a macro has been recorded`() {
        myFixture.configureByText(FILE, "<caret>foo")
        installPlayback()

        assertTrue(myFixture.testAction(runLastMacro()).isEnabled)
    }

    @Test
    fun `Run last macro is disabled while a macro is playing`() {
        myFixture.configureByText(FILE, "<caret>foo")
        installPlayback().isPlaying = true

        assertFalse(myFixture.testAction(runLastMacro()).isEnabled)
    }

    @Test
    fun `Run last macro is disabled outside an editor`() {
        myFixture.configureByText(FILE, "<caret>foo")
        installPlayback()

        val event =
            AnActionEvent.createEvent(
                runLastMacro(),
                SimpleDataContext.getProjectContext(myFixture.project),
                null,
                ActionPlaces.KEYBOARD_SHORTCUT,
                ActionUiKind.NONE,
                null,
            )
        ActionUtil.updateAction(runLastMacro(), event)

        assertFalse(event.presentation.isEnabled)
    }

    @Test
    fun `Run last macro runs the macro once`() {
        myFixture.configureByText(FILE, "<caret>")
        installPlayback()

        myFixture.performEditorAction(ACTION_RUN_LAST_MACRO)
        awaitRepeatFinished()

        assertEquals(1, playback.runs)
        myFixture.checkResult("x<caret>")
    }

    @Test
    fun `Universal argument runs the macro that many times, one run after the other`() {
        myFixture.configureByText(FILE, "<caret>")
        installPlayback()

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("3")
        myFixture.performEditorAction(ACTION_RUN_LAST_MACRO)
        awaitRepeatFinished()

        assertEquals(3, playback.runs)
        assertFalse(playback.hasOverlapped)
        myFixture.checkResult("xxx<caret>")
    }

    @Test
    fun `Cancelling the repeat stops the remaining runs of the macro`() {
        myFixture.configureByText(FILE, "<caret>")
        installPlayback()

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.performEditorAction(ACTION_RUN_LAST_MACRO)
        PlatformTestUtil.waitWithEventsDispatching("The macro never started", { playback.runs == 1 }, TIMEOUT_SECONDS)
        myFixture.performEditorAction(ACTION_CANCEL_REPEAT)
        settle()

        assertEquals(1, playback.runs)
        assertFalse(EmacsJService.instance.isRepeating())
        // The run that had already started is not interrupted.
        myFixture.checkResult("x<caret>")
    }

    private fun installPlayback(): FakePlayback {
        playback = FakePlayback { myFixture.type("x") }
        ActionManager.getInstance().replaceAction(PLAYBACK_LAST_MACRO, playback)
        return playback
    }

    private fun runLastMacro(): AnAction = ActionManager.getInstance().getAction(ACTION_RUN_LAST_MACRO)

    private fun awaitRepeatFinished() {
        PlatformTestUtil.waitWithEventsDispatching(
            "The macro repeat did not finish",
            { !EmacsJService.instance.isRepeating() },
            TIMEOUT_SECONDS,
        )
    }

    /** Dispatches events for a while, giving runs that should not happen the chance to. */
    private fun settle() {
        val until = System.currentTimeMillis() + SETTLE_MILLIS
        PlatformTestUtil.waitWithEventsDispatching("Settling timed out", { System.currentTimeMillis() > until }, TIMEOUT_SECONDS)
    }
}

/**
 * Stands in for the platform's `PlaybackLastMacro`, which cannot play a macro headlessly. Like the real action it is enabled
 * when a macro exists and none is playing, and its playback completes asynchronously, after [actionPerformed] has returned.
 */
private class FakePlayback(private val play: () -> Unit) : AnAction() {

    var hasMacro = true

    var isPlaying = false

    var runs = 0

    /** Set if a run was started while the previous one was still playing. */
    var hasOverlapped = false

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = hasMacro && !isPlaying
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (isPlaying) {
            hasOverlapped = true
        }
        isPlaying = true
        runs++
        ApplicationManager.getApplication().invokeLater {
            play()
            isPlaying = false
        }
    }
}
