package com.github.strindberg.emacsj.zap

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ESCAPE
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_ZAP_FORWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapto"
private const val ACTION_ZAP_FORWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapupto"
private const val ACTION_ZAP_BACKWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapbackto"
private const val ACTION_ZAP_BACKWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapbackupto"

class ZapTest : BasePlatformTestCase() {

    override fun tearDown() {
        ZapHandler.delegate?.hide()
        super.tearDown()
    }

    fun `test Zap up to works`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("l")

        myFixture.checkResult("bar <caret>l baz")
    }

    fun `test Zap up to works with no match`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("q")

        myFixture.checkResult("bar <caret>")
    }

    fun `test Zap up to works when matching next char`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret>fool baz")
    }

    fun `test Zap up to works at buffer end`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("z")

        myFixture.checkResult("bar <caret>z")
    }

    fun `test Zap to works`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("l")

        myFixture.checkResult("bar <caret> baz")
    }

    fun `test Zap to works with no match`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("q")

        myFixture.checkResult("bar <caret>")
    }

    fun `test Zap to works when matching next char`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret>ool baz")
    }

    fun `test Zap to works at buffer end`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("z")

        myFixture.checkResult("bar <caret>")
    }

    fun `test Zap back up to works`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("f")

        myFixture.checkResult("bar f<caret> baz")
    }

    fun `test Zap back up to works with no match`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("q")

        myFixture.checkResult("<caret> baz")
    }

    fun `test Zap back up to works when matching previous char`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("l")

        myFixture.checkResult("bar fool<caret> baz")
    }

    fun `test Zap back up to works at buffer start`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("b")

        myFixture.checkResult("b<caret> baz")
    }

    fun `test Zap back to works`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret> baz")
    }

    fun `test Zap back to works with no match`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("q")

        myFixture.checkResult("<caret> baz")
    }

    fun `test Zap back to works when matching previous char`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("l")

        myFixture.checkResult("bar foo<caret> baz")
    }

    fun `test Zap back to works at buffer start`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("b")

        myFixture.checkResult("<caret> baz")
    }

    fun `test Zap with capital letter only matches capital letters`() {
        myFixture.configureByText(FILE, "<caret>bar foo Foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("F")

        myFixture.checkResult("<caret>oo")
    }

    fun `test Zap with lower-case letter matches capital letters`() {
        myFixture.configureByText(FILE, "<caret>bar Foo foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("F")

        myFixture.checkResult("<caret>oo foo")
    }

    fun `test Pressing escape aborts`() {
        myFixture.configureByText(FILE, "<caret>bar Foo foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        pressEscape()

        myFixture.checkResult("<caret>bar Foo foo")
    }

    private fun pressEscape() {
        val popup = ZapHandler.delegate?.ui?.popup
        val textField = ZapHandler.delegate?.ui?.textField
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        ZapHandler.delegate?.hide()
    }
}
