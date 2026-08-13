package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.StringSelection
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_A
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import java.awt.event.KeyEvent.VK_N
import java.awt.event.KeyEvent.VK_P
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_COPY
import com.intellij.openapi.ide.CopyPasteManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val FILE = "killringfile.txt"

class KillRingTest : EmacsJTestCase() {

    @Test
    fun `Paste history without a preceding paste opens the kill ring`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)

        assertNotNull(PasteHandler.killRingDelegate)
        assertEquals(2, PasteHandler.killRingDelegate?.ui?.list?.model?.size)
        assertEquals(0, PasteHandler.killRingDelegate?.selectedIndex)
    }

    @Test
    fun `Enter pastes the selected entry and closes the popup`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        // Moving within the list is the popup's own doing, so the choice is made directly here.
        PasteHandler.killRingDelegate!!.selectedIndex = 1
        press(VK_ENTER)

        assertNull(PasteHandler.killRingDelegate)
        myFixture.checkResult("older<caret>")
    }

    @Test
    fun `Escape closes the popup without pasting`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        press(VK_ESCAPE)

        assertNull(PasteHandler.killRingDelegate)
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `Ctrl-G closes the popup without pasting`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        pressCtrl(VK_G)

        assertNull(PasteHandler.killRingDelegate)
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `The whole entry is pasted even though the list shows one line`() {
        resetClipboard("has\nseveral\nlines")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        // What the list draws is a single line; what gets pasted is not.
        assertEquals("has\u21b5several\u21b5lines", PasteHandler.killRingDelegate?.ui?.list?.model?.getElementAt(0))

        press(VK_ENTER)

        myFixture.checkResult("has\nseveral\nlines<caret>")
    }

    @Test
    fun `A paste followed by paste history still cycles instead of opening the ring`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)

        assertNull(PasteHandler.killRingDelegate)
        myFixture.checkResult("older<caret>")
    }

    @Test
    fun `A line copied whole is pasted at the caret, not as its own line`() {
        (CopyPasteManager.getInstance() as CopyPasteManagerEx).let { m -> m.allContents.forEach { m.removeContent(it) } }
        myFixture.configureByText(FILE, "alpha\nbe<caret>ta\ngamma")
        myFixture.performEditorAction(ACTION_EDITOR_COPY)

        myFixture.configureByText(FILE, "X<caret>Y")
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        press(VK_ENTER)

        // The transferable carries caret-state and paste-option flavors that would otherwise make the platform
        // insert it above as a line of its own.
        myFixture.checkResult("Xbeta\n<caret>Y")
    }

    @Test
    fun `Only the keys that finish the choice are claimed`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)

        assertTrue(consumed(VK_ENTER, 0))
        assertTrue(consumed(VK_ESCAPE, 0))
        assertTrue(consumed(VK_G, CTRL_DOWN_MASK))

        // Navigation has to be left to the popup itself. Claiming ctrl-n and ctrl-p here as well is what made the
        // selection move two rows for every press.
        assertFalse(consumed(VK_N, CTRL_DOWN_MASK))
        assertFalse(consumed(VK_P, CTRL_DOWN_MASK))
        assertFalse(consumed(VK_A, 0))
    }

    @Test
    fun `A universal argument leaves the caret in front of the pasted entry`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "start<caret>end")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        PasteHandler.killRingDelegate!!.selectedIndex = 1
        press(VK_ENTER)

        assertNull(PasteHandler.killRingDelegate)
        myFixture.checkResult("start<caret>olderend")
    }

    @Test
    fun `Without a universal argument the caret follows the pasted entry`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "start<caret>end")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        PasteHandler.killRingDelegate!!.selectedIndex = 1
        press(VK_ENTER)

        myFixture.checkResult("startolder<caret>end")
    }

    @Test
    fun `A normal paste leaves the mark before the pasted entry`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "start<caret>end")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        PasteHandler.killRingDelegate!!.selectedIndex = 1
        press(VK_ENTER)
        myFixture.checkResult("startolder<caret>end")

        myFixture.performEditorAction(ACTION_POP_MARK)

        myFixture.checkResult("start<caret>olderend")
    }

    @Test
    fun `A prefix paste leaves the mark after the pasted entry`() {
        resetClipboard("older", "newer")
        myFixture.configureByText(FILE, "start<caret>end")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        PasteHandler.killRingDelegate!!.selectedIndex = 1
        press(VK_ENTER)
        myFixture.checkResult("start<caret>olderend")

        myFixture.performEditorAction(ACTION_POP_MARK)

        myFixture.checkResult("startolder<caret>end")
    }

    private fun press(keyCode: Int) {
        dispatch(keyCode, 0)
    }

    private fun pressCtrl(keyCode: Int) {
        dispatch(keyCode, CTRL_DOWN_MASK)
    }

    /**
     * Whether the popup's own key handler claims the event rather than passing it on. Asked of a key release, which
     * is claimed just as a press is but acts on nothing, so one popup can answer for every key in turn.
     */
    private fun consumed(keyCode: Int, modifiers: Int): Boolean {
        val ui = PasteHandler.killRingDelegate!!.ui
        return ui.popup.dispatchKeyEvent(KeyEvent(ui.list, KeyEvent.KEY_RELEASED, 1234L, modifiers, keyCode, CHAR_UNDEFINED))
    }

    private fun dispatch(keyCode: Int, modifiers: Int) {
        val ui = PasteHandler.killRingDelegate!!.ui
        ui.popup.dispatchKeyEvent(KeyEvent(ui.list, KeyEvent.KEY_PRESSED, 1234L, modifiers, keyCode, CHAR_UNDEFINED))
    }

    private fun resetClipboard(vararg items: String) {
        (CopyPasteManager.getInstance() as CopyPasteManagerEx).let { manager ->
            manager.allContents.forEach { manager.removeContent(it) }
            items.forEach { manager.setContents(StringSelection(it)) }
        }
    }
}
