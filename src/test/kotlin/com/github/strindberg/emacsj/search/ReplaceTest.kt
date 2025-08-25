package com.github.strindberg.emacsj.search

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_START
import com.intellij.testFramework.fixtures.BasePlatformTestCase

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

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("bar<caret>")
    }

    fun `test Yes and no while replacing works`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("null")
        pressEnter()

        setText("\"label\"")
        pressEnter()

        typeChar('y')
        typeChar('y')
        typeChar('n')

        myFixture.checkResult(""""label" () "label" () null<caret>""")
    }

    fun `test Space and no while replacing works`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("null")
        pressEnter()

        setText("\"label\"")
        pressEnter()

        typeChar(' ')
        typeChar(' ')
        typeChar('n')

        myFixture.checkResult(""""label" () "label" () null<caret>""")
    }

    fun `test Different order of yes and no works`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')
        typeChar('n')
        typeChar('y')

        myFixture.checkResult("bar foo bar<caret>")
    }

    fun `test Replacement is only done within selection`() {
        myFixture.configureByText(FILE, "<caret><selection>foo foo</selection> foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')
        typeChar('y')

        ReplaceHandler.delegate!!.hide()

        myFixture.checkResult("bar bar<caret> foo")
    }

    fun `test Replacement is over after period`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')
        typeChar('.')

        myFixture.checkResult("bar bar<caret> foo")
    }

    fun `test Exclamation mark replaces everything and with correct case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("bar Bar BAR<caret>")
    }

    fun `test Upper case in replacement makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("BAR")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("BAR<caret> Foo FOO")
    }

    fun `test An upper case letter in source makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("Foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("foo bar<caret> FOO")
    }

    fun `test An upper case word can be transformed to lower case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("Foo")
        pressEnter()

        setText("foo")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("foo foo<caret> FOO")
    }

    fun `test A lower case word can be transformed to upper case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("Foo")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("Foo<caret> Foo FOO")
    }

    fun `test Regexp replace with back references java style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("$1å$2")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("båt<caret>")
    }

    fun `test Backslash before dollar stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\$1å\$2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""$1å$2<caret>""")
    }

    fun `test Regexp replace with back references traditional style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\1å\2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("båt<caret>")
    }

    fun `test Double escape stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\\1å\\2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""\1å\2<caret>""")
    }

    fun `test Replace whole regexp match java style works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("a")
        pressEnter()

        setText("$0$0")
        pressEnter()

        typeChar('y')
        typeChar('y')

        myFixture.checkResult("baaaa<caret>t")
    }

    fun `test Replace whole regexp match traditional style works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("a")
        pressEnter()

        setText("""\&\&""")
        pressEnter()

        typeChar('y')
        typeChar('y')

        myFixture.checkResult("baaaa<caret>t")
    }

    fun `test Double escape stops back reference to whole match in regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("aaa")
        pressEnter()

        setText("""\\&\\&""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""b\&\&<caret>t""")
    }

    fun `test Replace whole regexp match traditional style and exclamation mark works`() {
        myFixture.configureByText(FILE, "<caret>baat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("a")
        pressEnter()

        setText("""\&\&""")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("baaaa<caret>t")
        ReplaceHandler.delegate = null
    }

    fun `test Regexp replace works with simple text replace`() {
        myFixture.configureByText(FILE, "<caret>aa")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("a")
        pressEnter()

        setText("b")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("bb<caret>")
    }

    fun `test Mark is set when replace starts`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("null")
        pressEnter()

        setText("\"label\"")
        pressEnter()

        typeChar('y')
        typeChar('y')
        typeChar('n')

        myFixture.checkResult(""""label" () "label" () null<caret>""")
        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("""<caret>"label" () "label" () null""")
    }

    fun `test Previous replace commands can be reused`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()
        setText("bar")
        pressEnter()
        typeChar('y')

        myFixture.checkResult("bar<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("bar")
        pressEnter()
        setText("foo")
        pressEnter()
        typeChar('y')

        myFixture.checkResult("foo<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        pressEnter()
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        pressEnter()
        typeChar('y')

        myFixture.checkResult("bar<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)
    }

    fun `test Next item in replace history works as intended`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("o")
        pressEnter()
        setText("a")
        pressEnter()
        typeChar('!')

        myFixture.checkResult("faa<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("a")
        pressEnter()
        setText("aa")
        pressEnter()
        typeChar('!')

        myFixture.checkResult("faaaa<caret>")

        ReplaceHandler.delegate?.hide()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_START)

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_NEXT)
        pressEnter()
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_PREVIOUS)
        myFixture.performEditorAction(ACTION_REPLACE_NEXT)
        pressEnter()
        typeChar('!')

        myFixture.checkResult("faaaaaaaa<caret>")
    }

    fun `test Previous replace command can be accepted with ENTER`() {
        myFixture.configureByText(FILE, "<caret>foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()
        setText("bar")
        pressEnter()
        typeChar('.')

        myFixture.checkResult("bar<caret> foo")
        ReplaceHandler.delegate?.hide()

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        pressEnter()
        typeChar('.')

        myFixture.checkResult("bar bar<caret>")
    }

    fun `test New line character can be added to search string`() {
        myFixture.configureByText(
            FILE,
            """
            |<caret>foo foo
            |foo bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        myFixture.performEditorAction(ACTION_REPLACE_NEWLINE)
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('!')

        myFixture.checkResult(
            """
            |foo bar<caret>foo bar
            """.trimMargin()
        )
    }

    fun `test New line character can be added to replacement string`() {
        myFixture.configureByText(
            FILE,
            """
            |<caret>foo foo
            |foo bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        myFixture.performEditorAction(ACTION_REPLACE_NEWLINE)
        pressEnter()

        typeChar('!')

        myFixture.checkResult(
            """
            |bar
            | bar
            |
            |bar
            |<caret> bar
            """.trimMargin()
        )
    }

    fun `test Undo replacing works`() {
        myFixture.configureByText(FILE, "<caret>null () null () null null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("null")
        pressEnter()

        setText("\"label\"")
        pressEnter()

        typeChar('y')
        typeChar('n')
        typeChar('y')

        myFixture.checkResult(""""label" () null () "label" null<caret>""")

        typeChar('u')

        myFixture.checkResult(""""label" () null () null<caret> null""")

        typeChar('u')

        myFixture.checkResult("""null<caret> () null () null null""")
    }

    fun `test Replacements can be visited`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("foo")
        pressEnter()

        typeChar('y')
        typeChar('y')
        typeChar('y')

        myFixture.checkResult("foo foo foo foo<caret>")

        typeChar('^')

        myFixture.checkResult("foo foo foo<caret> foo")

        typeChar('^')

        myFixture.checkResult("foo foo<caret> foo foo")

        typeChar('^')

        myFixture.checkResult("foo<caret> foo foo foo")
    }

    fun `test Replacement can be edited`() {
        myFixture.configureByText(FILE, "<caret>null () null () null")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("null")
        pressEnter()

        setText("\"label\"")
        pressEnter()

        typeChar('y')

        myFixture.checkResult(""""label" () null<caret> () null""")

        typeChar('e')

        setText("other")
        pressEnter()

        myFixture.checkResult(""""label" () other () null<caret>""")

        typeChar('y')

        myFixture.checkResult(""""label" () other () other<caret>""")
    }

    private fun setText(text: String) {
        ReplaceHandler.delegate!!.ui.text = (text)
    }

    private fun pressEnter() {
        val textField = ReplaceHandler.delegate!!.ui.textField
        ReplaceHandler.delegate!!.ui.popup.apply {
            dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
            dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
        }
    }

    private fun typeChar(char: Char) {
        val textField = ReplaceHandler.delegate!!.ui.textField
        ReplaceHandler.delegate!!.ui.popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_TYPED, 1234L, 0, KeyEvent.VK_UNDEFINED, char))
    }
}
