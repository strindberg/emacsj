package com.github.strindberg.emacsj.universal

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ESCAPE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class UniversalArgumentTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        UniversalArgumentDelegate.testing = true
    }

    override fun tearDown() {
        UniversalArgumentHandler.delegate?.hide()
        super.tearDown()
    }

    fun `test Universal argument before movement moves four steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        myFixture.checkResult("foob<caret>ar")
    }

    fun `test Universal argument with '5' before movement moves five steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        myFixture.checkResult("fooba<caret>r")
    }

    fun `test First non-digit after Universal argument triggers action`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.type("a")
        myFixture.checkResult("aaaaa<caret>")
    }

    fun `test Multiple digits are interpreted as number`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("1")
        myFixture.type("5")
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaaaaaaaaa<caret>")
    }

    fun `test Repeated Universal argument multiplies by four`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaaaaaaaaaa<caret>")
    }

    fun `test Pressing 'Escape' aborts universal argument`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        pressEscape()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        myFixture.checkResult("f<caret>oobar")
    }

    fun `test Numeric universal arguments work`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("a")
        myFixture.checkResult("a<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.type("a")
        myFixture.checkResult("aa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.type("a")
        myFixture.checkResult("aaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT4)
        myFixture.type("a")
        myFixture.checkResult("aaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.type("a")
        myFixture.checkResult("aaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT6)
        myFixture.type("a")
        myFixture.checkResult("aaaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT7)
        myFixture.type("a")
        myFixture.checkResult("aaaaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT8)
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT9)
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaaa<caret>")
    }

    fun `test Numeric universal argument 10 works two ways`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("0")
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaaaa<caret>")
        UniversalArgumentHandler.delegate?.hide()

        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.type("a")
        myFixture.checkResult("aaaaaaaaaa<caret>")
    }

    private fun pressEscape() {
        val popup = UniversalArgumentHandler.delegate?.ui?.popup
        val textField = UniversalArgumentHandler.delegate?.ui?.textField
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        UniversalArgumentHandler.delegate?.hide()
    }
}
