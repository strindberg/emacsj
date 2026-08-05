package com.github.strindberg.emacsj.universal

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
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
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("a")
        checkResult("a<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>a")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.type("a")
        checkResult("aa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.type("a")
        checkResult("aaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT4)
        myFixture.type("a")
        checkResult("aaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT4)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.type("a")
        checkResult("aaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT6)
        myFixture.type("a")
        checkResult("aaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT6)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT7)
        myFixture.type("a")
        checkResult("aaaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT7)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT8)
        myFixture.type("a")
        checkResult("aaaaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT8)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaaaa")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT9)
        myFixture.type("a")
        checkResult("aaaaaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT9)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaaaaa")
    }

    fun `test Numeric universal argument 10 works two ways`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("0")
        myFixture.type("a")
        checkResult("aaaaaaaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

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
        UniversalArgumentHandler.delegate?.run {
            val popup = ui.popup
            val textField = ui.textField
            popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
            popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        }
        UniversalArgumentHandler.delegate?.hide()
    }
}
