package com.github.strindberg.emacsj.search

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_START
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.ui.JBColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FILE = "replacefile.txt"

class ReplaceTest : EmacsJTestCase() {

    @BeforeEach
    fun speedUpHighlighting() {
        CommonHighlighter.instance.delayMillis = 0
    }

    @AfterEach
    fun restoreHighlightingDelay() {
        CommonHighlighter.instance.delayMillis = HIGHLIGHT_DELAY_MILLIS
    }

    @Test
    fun `Simple text replace works`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("bar<caret>")
    }

    @Test
    fun `Yes and no while replacing works`() {
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

    @Test
    fun `Space and no while replacing works`() {
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

    @Test
    fun `Different order of yes and no works`() {
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

    @Test
    fun `Replacement is only done within selection`() {
        myFixture.configureByText(FILE, "<caret><selection>foo foo</selection> foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')
        typeChar('y')

        myFixture.checkResult("bar bar<caret> foo")
    }

    @Test
    fun `Replacement is over after period`() {
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

    @Test
    fun `Exclamation mark replaces everything and with correct case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("bar Bar BAR<caret>")
    }

    @Test
    fun `Upper case in replacement makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("BAR")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("BAR<caret> Foo FOO")
    }

    @Test
    fun `An upper case letter in source makes replacement dependent on case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("Foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("foo bar<caret> FOO")
    }

    @Test
    fun `An upper case word can be transformed to lower case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("Foo")
        pressEnter()

        setText("foo")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("foo foo<caret> FOO")
    }

    @Test
    fun `A lower case word can be transformed to upper case`() {
        myFixture.configureByText(FILE, "<caret>foo Foo FOO")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("Foo")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("Foo<caret> Foo FOO")
    }

    @Test
    fun `Regexp replace with back references java style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("$1å$2")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("båt<caret>")
    }

    @Test
    fun `Backslash before dollar stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\$1å\$2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""$1å$2<caret>""")
    }

    @Test
    fun `Regexp replace with back references traditional style works`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\1å\2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("båt<caret>")
    }

    @Test
    fun `Double escape stops back reference regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("(.)aaa(.)")
        pressEnter()

        setText("""\\1å\\2""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""\1å\2<caret>""")
    }

    @Test
    fun `Replace whole regexp match java style works`() {
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

    @Test
    fun `Replace whole regexp match traditional style works`() {
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

    @Test
    fun `Escaped backslash before a back reference keeps both`() {
        // "\\" is a literal backslash and the "\1" that follows it is still a back reference, so the replacement
        // should produce a backslash followed by the first group.
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("""(.)aaa(.)""")
        pressEnter()

        setText("""\\\1""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""\b<caret>""")
    }

    @Test
    fun `Escaped backslash before a whole match reference keeps both`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("""aaa""")
        pressEnter()

        setText("""\\\&""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""b\aaa<caret>t""")
    }

    @Test
    fun `Double escape stops back reference to whole match in regexp replace`() {
        myFixture.configureByText(FILE, "<caret>baaat")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("aaa")
        pressEnter()

        setText("""\\&\\&""")
        pressEnter()

        typeChar('y')

        myFixture.checkResult("""b\&\&<caret>t""")
    }

    @Test
    fun `Replace whole regexp match traditional style and exclamation mark works`() {
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

    @Test
    fun `Regexp replace works with simple text replace`() {
        myFixture.configureByText(FILE, "<caret>aa")
        myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

        setText("a")
        pressEnter()

        setText("b")
        pressEnter()

        typeChar('!')

        myFixture.checkResult("bb<caret>")
    }

    @Test
    fun `Mark is set when replace starts`() {
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

    @Test
    fun `Previous replace commands can be reused`() {
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

    @Test
    fun `Next item in replace history works as intended`() {
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

    @Test
    fun `Previous replace command can be accepted with ENTER`() {
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

    @Test
    fun `New line character can be added to search string`() {
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

    @Test
    fun `New line character can be added to replacement string`() {
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

    @Test
    fun `Undo replacing works`() {
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

    @Test
    fun `Replacements can be visited`() {
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

    @Test
    fun `Replacement can be edited`() {
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

    @Test
    fun `Invoking text replace while searching forward works`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REPLACE_TEXT)

        setText("bar")
        pressEnter()

        typeChar('y')
        pressEnter()

        myFixture.checkResult("bar foo<caret> foo")
    }

    @Test
    fun `Invoking text replace while searching backward works`() {
        myFixture.configureByText(FILE, "foo foo foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo <caret>foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REPLACE_TEXT)

        setText("bar")
        pressEnter()

        typeChar('y')
        pressEnter()

        myFixture.checkResult("foo bar foo<caret>")
    }

    @Test
    fun `Invoking regexp replace while searching forward works`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.type("fo+")
        myFixture.checkResult("foo<caret> foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REPLACE_REGEXP)

        setText("baz")
        pressEnter()

        typeChar('y')
        typeChar('y')
        pressEnter()

        myFixture.checkResult("baz baz foo<caret>")
    }

    @Test
    fun `Invoking regexp replace while searching backward works`() {
        myFixture.configureByText(FILE, "foo foo foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.type("fo+")
        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.checkResult("<caret>foo foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REPLACE_REGEXP)

        setText("baz")
        pressEnter()

        typeChar('y')
        typeChar('y')
        pressEnter()

        myFixture.checkResult("baz baz foo<caret>")
    }

    @Test
    fun `Adding a caret cancels the replace session`() {
        myFixture.configureByText(FILE, "<caret>foo\nbar")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)
        assertNotNull(ReplaceHandler.delegate)

        myFixture.editor.caretModel.addCaret(VisualPosition(1, 0))

        assertNull(ReplaceHandler.delegate)
    }

    @Test
    fun `Replace reduces multiple carets to one`() {
        myFixture.configureByText(FILE, "fo<caret>o\nba<caret>r")
        assertEquals(2, myFixture.editor.caretModel.caretCount)

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        assertEquals(1, myFixture.editor.caretModel.caretCount)
    }

    @Test
    fun `Replace is disabled in an editor without a project`() {
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("foo bar baz"))
        try {
            assertNull(editor.project)
            val handler = ReplaceHandler(type = SearchType.TEXT)

            assertFalse(handler.isEnabled(editor, editor.caretModel.primaryCaret, DataContext.EMPTY_CONTEXT))

            // FindManager is a project service, so starting a replace here would fail on the first typed character.
            handler.execute(editor, null, DataContext.EMPTY_CONTEXT)
            assertNull(ReplaceHandler.delegate)
        } finally {
            factory.releaseEditor(editor)
        }
    }

    @Test
    fun `Malformed search regexp matches nothing`() {
        ["(", "[a", """a\"""].forEach { malformed ->
            myFixture.configureByText(FILE, "<caret>aaa bbb")
            myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

            setText(malformed)
            pressEnter()
            setText("x")
            pressEnter()

            assertEquals("Replaced 0 occurrences.", ReplaceHandler.delegate!!.ui.title)
            assertEquals("aaa bbb", myFixture.editor.document.text)

            ReplaceHandler.delegate?.hide()
        }
    }

    @Test
    fun `Malformed replacement reports a failed replacement`() {
        ["""\2""", """\9""", "$", "\$x", """y\"""].forEach { malformed ->
            myFixture.configureByText(FILE, "<caret>aaa bbb")
            myFixture.performEditorAction(ACTION_REPLACE_REGEXP)

            setText("(a)")
            pressEnter()
            setText(malformed)
            pressEnter()
            typeChar('y')

            assertEquals("Replacement failed. ", ReplaceHandler.delegate!!.ui.title)
            assertEquals(JBColor.RED, ReplaceHandler.delegate!!.ui.textColor)
            assertEquals("aaa bbb", myFixture.editor.document.text)

            ReplaceHandler.delegate?.hide()
        }
    }

    @Test
    fun `A replace leaves highlights belonging to the rest of the IDE alone`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        val foreign = myFixture.editor.markupModel.addRangeHighlighter(
            EditorColors.SEARCH_RESULT_ATTRIBUTES,
            4,
            7,
            HighlighterLayer.LAST,
            HighlighterTargetArea.EXACT_RANGE
        )

        myFixture.performEditorAction(ACTION_REPLACE_TEXT)
        setText("foo")
        pressEnter()
        setText("baz")
        pressEnter()
        typeChar('y')
        assertTrue(foreign.isValid, "cleared while replacing")

        typeChar('.')

        assertTrue(foreign.isValid, "cleared when the replace ended")
        assertTrue(myFixture.editor.markupModel.allHighlighters.contains(foreign))
    }

    @Test
    fun `Comma replaces the match without moving to the next`() {
        myFixture.configureByText(FILE, "<caret>foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar(',')

        // Replaced, but the session stays where it is. The caret is what distinguishes this from `y`, which
        // replaces and moves on: `y` would leave it at the second occurrence.
        myFixture.checkResult("bar<caret> foo")
        assertNotNull(ReplaceHandler.delegate)
    }

    @Test
    fun `Ctrl-L leaves the replace session untouched`() {
        myFixture.configureByText(FILE, "<caret>foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('\u000c')

        // The recentering itself cannot be asserted headlessly -- the fixture reports a zero-sized viewport -- but
        // a missing action or a malformed AnActionEvent would surface here, and the session must survive either way.
        assertEquals("foo foo", myFixture.editor.document.text)
        assertNotNull(ReplaceHandler.delegate)

        typeChar('y')

        assertEquals("bar foo", myFixture.editor.document.text)
    }

    @Test
    fun `An unrecognized key ends the replace session without replacing`() {
        myFixture.configureByText(FILE, "<caret>foo foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('q')

        assertNull(ReplaceHandler.delegate)
        assertEquals("foo foo", myFixture.editor.document.text)
    }

    @Test
    fun `A key press dismisses the popup once the last match has been replaced`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_REPLACE_TEXT)

        setText("foo")
        pressEnter()

        setText("bar")
        pressEnter()

        typeChar('y')

        // Nothing left to replace, so the popup stays up reporting what it did until a key dismisses it.
        assertNotNull(ReplaceHandler.delegate)

        pressKey(ReplaceHandler.delegate?.ui, KeyEvent.VK_A)

        assertNull(ReplaceHandler.delegate)
    }

    private fun setText(text: String) {
        ReplaceHandler.delegate!!.ui.text = text
    }

    private fun pressEnter() {
        pressKey(ReplaceHandler.delegate?.ui, VK_ENTER)
    }

    private fun typeChar(char: Char) {
        val textField = ReplaceHandler.delegate!!.ui.textField
        ReplaceHandler.delegate!!.ui.popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_TYPED, 1234L, 0, KeyEvent.VK_UNDEFINED, char))
    }
}
