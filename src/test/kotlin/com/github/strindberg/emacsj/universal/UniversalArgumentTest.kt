package com.github.strindberg.emacsj.universal

import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_DELETE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_UNDO
import com.intellij.testFramework.PlatformTestUtil

private const val FILE = "universalfile.txt"

class UniversalArgumentTest : EmacsJTestCase() {

    fun `test Universal argument before movement moves four steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("foob<caret>ar")
    }

    fun `test Universal argument with '5' before movement moves five steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("fooba<caret>r")
    }

    fun `test First non-digit after Universal argument triggers action`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.type("a")
        checkResult("aaaaa<caret>")
    }

    fun `test Multiple digits are interpreted as number`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("1")
        myFixture.type("5")
        myFixture.type("a")
        checkResult("aaaaaaaaaaaaaaa<caret>")
    }

    fun `test Repeated Universal argument multiplies by four`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("a")
        checkResult("aaaaaaaaaaaaaaaa<caret>")
    }

    fun `test Pressing 'Escape' aborts universal argument`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        pressEscape()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("f<caret>oobar")
    }

    fun `test Numeric universal arguments work`() {
        [
            ACTION_UNIVERSAL_ARGUMENT1 to 1,
            ACTION_UNIVERSAL_ARGUMENT2 to 2,
            ACTION_UNIVERSAL_ARGUMENT3 to 3,
            ACTION_UNIVERSAL_ARGUMENT4 to 4,
            ACTION_UNIVERSAL_ARGUMENT5 to 5,
            ACTION_UNIVERSAL_ARGUMENT6 to 6,
            ACTION_UNIVERSAL_ARGUMENT7 to 7,
            ACTION_UNIVERSAL_ARGUMENT8 to 8,
            ACTION_UNIVERSAL_ARGUMENT9 to 9
        ].forEach { (action, times) ->
            myFixture.configureByText(FILE, "<caret>")

            myFixture.performEditorAction(action)
            myFixture.type("a")
            checkResult("a".repeat(times) + "<caret>")

            myFixture.performEditorAction(action)
            myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
            checkResult("<caret>" + "a".repeat(times))

            UniversalArgumentHandler.delegate?.hide()
        }
    }

    fun `test Numeric universal argument 10 works 1`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("0")
        myFixture.type("a")
        checkResult("aaaaaaaaaa<caret>")
    }

    fun `test Numeric universal argument 10 works 2`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.type("a")
        checkResult("aaaaaaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaaaaaa")
    }

    fun `test A repeat larger than the batch size runs every repetition`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("5")
        myFixture.type("0")
        myFixture.type("a")
        checkResult("a".repeat(150) + "<caret>")
    }

    fun `test Repeating is switched off once the repeat has finished`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.type("a")

        runPendingRepeats()

        assertFalse(EmacsJService.instance.isRepeating())
    }

    fun `test Cancelling the repeat drops repetitions that have not run yet`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.type("a")

        myFixture.performEditorAction(ACTION_CANCEL_REPEAT)

        checkResult("<caret>")
    }

    fun `test Universal argument repeat applies to every caret`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>abcdef
                |<caret>abcdef
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)

        checkResult(
            """
                |abc<caret>def
                |abc<caret>def
            """.trimMargin()
        )
    }

    fun `test Universal argument before backspace deletes four characters`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)

        checkResult("abcd<caret>ijkl")
    }

    fun `test Universal argument before delete deletes four characters`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_DELETE)

        checkResult("abcdefgh<caret>")
    }

    fun `test A repeated deletion is undone in one step`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        checkResult("abcd<caret>ijkl")

        // The repetitions share one command group id, so they collapse into the single step the first press began.
        myFixture.performEditorAction(ACTION_UNDO)
        runPendingRepeats()
        assertEquals("abcdefghijkl", myFixture.editor.document.text)
    }

    /** Repeats of more than one are queued rather than run inline, so let them finish before asserting. */
    private fun checkResult(expected: String) {
        runPendingRepeats()
        myFixture.checkResult(expected)
    }

    /**
     * Runs whatever the universal-argument machinery has queued. Repeats are dispatched in batches through
     * `invokeLater` so that a long repeat stays interruptible, which means they have not run yet by the time the
     * triggering action returns.
     */
    private fun runPendingRepeats() {
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    private fun pressEscape() {
        pressKey(UniversalArgumentHandler.delegate?.ui, VK_ESCAPE)
        UniversalArgumentHandler.delegate?.hide()
    }
}
