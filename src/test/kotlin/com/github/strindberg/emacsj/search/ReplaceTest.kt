package com.github.strindberg.emacsj.search

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import javax.swing.JComponent
import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_START
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"
private const val ACTION_REPLACE_PREVIOUS = "com.github.strindberg.emacsj.actions.search.replaceprevious"

class ReplaceTest : BasePlatformTestCase() {

    override fun setUp() {
        CommonHighlighter.testing = true
        super.setUp()
    }

    override fun tearDown() {
        ReplaceHandler.delegate?.hide()
        super.tearDown()
    }

    fun `test Simple text replace works`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("bar<caret>")
    }

    fun `test Yes and no while replacing works`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "null"
        popup.pressEnter(textField)

        textField.text = "\"label\""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('y', textField)
        popup.typeChar('n', textField)

        myFixture.checkResult(""""label" () "label" () null<caret>""")
    }

    fun `test Space and no while replacing works`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "null"
        popup.pressEnter(textField)

        textField.text = "\"label\""
        popup.pressEnter(textField)

        popup.typeChar(' ', textField)
        popup.typeChar(' ', textField)
        popup.typeChar('n', textField)

        myFixture.checkResult(""""label" () "label" () null<caret>""")
    }

    fun `test Different order of yes and no works`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('n', textField)
        popup.typeChar('y', textField)

        myFixture.checkResult("bar foo bar<caret>")
    }

    fun `test Replacement is only done within selection`() {
        myFixture.configureByText(FILE, "<caret><selection>foo foo</selection> foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('y', textField)

        ReplaceHandler.delegate!!.hide()

        myFixture.checkResult("bar bar<caret> foo")
    }

    fun `test Replacement is over after period`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('.', textField)

        myFixture.checkResult("bar bar<caret> foo")
    }

    fun `test Exclamation mark replaces everything and with correct case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("bar Bar BAR<caret>")
    }

    fun `test Upper case in replacement makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "BAR"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("BAR<caret> Foo FOO")
    }

    fun `test An upper case letter in source makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "Foo"
        popup.pressEnter(textField)

        textField.text = "bar"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("foo bar<caret> FOO")
    }

    fun `test An upper case word can be transformed to lower case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "Foo"
        popup.pressEnter(textField)

        textField.text = "foo"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("foo foo<caret> FOO")
    }

    fun `test A lower case word can be transformed to upper case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)

        textField.text = "Foo"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("Foo<caret> Foo FOO")
    }

    fun `test Regexp replace with back references java style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "(.)aaa(.)"
        popup.pressEnter(textField)

        textField.text = "$1å$2"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("båt<caret>")
    }

    fun `test Backslash before dollar stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "(.)aaa(.)"
        popup.pressEnter(textField)

        textField.text = """\$1å\$2"""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("""$1å$2<caret>""")
    }

    fun `test Regexp replace with back references traditional style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "(.)aaa(.)"
        popup.pressEnter(textField)

        textField.text = """\1å\2"""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("båt<caret>")
    }

    fun `test Double escape stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "(.)aaa(.)"
        popup.pressEnter(textField)

        textField.text = """\\1å\\2"""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("""\1å\2<caret>""")
    }

    fun `test Replace whole regexp match java style works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "a"
        popup.pressEnter(textField)

        textField.text = "$0$0"
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('y', textField)

        myFixture.checkResult("baaaa<caret>t")
    }

    fun `test Replace whole regexp match traditional style works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "a"
        popup.pressEnter(textField)

        textField.text = """\&\&"""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('y', textField)

        myFixture.checkResult("baaaa<caret>t")
    }

    fun `test Double escape stops back reference to whole match in regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "aaa"
        popup.pressEnter(textField)

        textField.text = """\\&\\&"""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)

        myFixture.checkResult("""b\&\&<caret>t""")
    }

    fun `test Replace whole regexp match traditional style and exclamation mark works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "a"
        popup.pressEnter(textField)

        textField.text = """\&\&"""
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("baaaa<caret>t")
        ReplaceHandler.delegate = null
    }

    fun `test Regexp replace works with simple text replace`() {
        myFixture.configureByText(FILE, "<caret>aa")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "a"
        popup.pressEnter(textField)

        textField.text = "b"
        popup.pressEnter(textField)

        popup.typeChar('!', textField)

        myFixture.checkResult("bb<caret>")
    }

    fun `test Mark is set when replace starts`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "null"
        popup.pressEnter(textField)

        textField.text = "\"label\""
        popup.pressEnter(textField)

        popup.typeChar('y', textField)
        popup.typeChar('y', textField)
        popup.typeChar('n', textField)

        myFixture.checkResult(""""label" () "label" () null<caret>""")
        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("""<caret>"label" () "label" () null""")
    }

    fun `test Previous replace commands can be reused`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)
        textField.text = "bar"
        popup.pressEnter(textField)
        popup.typeChar('y', textField)

        myFixture.checkResult("bar<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)
        myFixture.checkResult("<caret>bar")

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField2 = ReplaceHandler.delegate!!.ui.textField
        val popup2 = ReplaceHandler.delegate!!.ui.popup

        textField2.text = "bar"
        popup2.pressEnter(textField2)
        textField2.text = "foo"
        popup2.pressEnter(textField2)
        popup2.typeChar('y', textField2)

        myFixture.checkResult("foo<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)
        myFixture.checkResult("<caret>foo")

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField3 = ReplaceHandler.delegate!!.ui.textField
        val popup3 = ReplaceHandler.delegate!!.ui.popup

        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        popup3.pressEnter(textField3)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        popup3.pressEnter(textField3)
        popup3.typeChar('y', textField3)

        myFixture.checkResult("bar<caret>")
    }

    fun `test Previous replace command can be accepted with ENTER`() {
        myFixture.configureByText(FILE, "<caret>foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField = ReplaceHandler.delegate!!.ui.textField
        val popup = ReplaceHandler.delegate!!.ui.popup

        textField.text = "foo"
        popup.pressEnter(textField)
        textField.text = "bar"
        popup.pressEnter(textField)
        popup.typeChar('.', textField)

        myFixture.checkResult("bar<caret> foo")
        ReplaceHandler.delegate?.hide()

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        val textField2 = ReplaceHandler.delegate!!.ui.textField
        val popup2 = ReplaceHandler.delegate!!.ui.popup

        popup2.pressEnter(textField2)
        popup2.typeChar('.', textField2)

        myFixture.checkResult("bar bar<caret>")
    }

    private fun JBPopup.pressEnter(component: JComponent) {
        dispatchKeyEvent(KeyEvent(component, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
        dispatchKeyEvent(KeyEvent(component, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
    }

    private fun JBPopup.typeChar(char: Char, component: JComponent) {
        dispatchKeyEvent(KeyEvent(component, KeyEvent.KEY_TYPED, 1234L, 0, KeyEvent.VK_UNDEFINED, char))
    }
}
