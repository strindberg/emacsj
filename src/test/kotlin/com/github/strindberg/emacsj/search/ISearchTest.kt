package com.github.strindberg.emacsj.search

import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_SHIFT
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FILE = "isearchfile.txt"

private const val HIGHLIGHT_TIMEOUT_SECONDS = 10

@Suppress("LargeClass", "ReplaceSafeCallChainWithRun")
class ISearchTest : EmacsJTestCase() {

    /** Match count as shown in the search UI, once debounced highlighting has reported it. */
    private val searchCount: Pair<Int, Int>?
        get() {
            waitForHighlighting()
            return ISearchHandler.delegate?.ui?.count
        }

    @BeforeEach
    fun speedUpHighlighting() {
        CommonHighlighter.delayMillis = 0
    }

    @AfterEach
    fun restoreHighlightingDelay() {
        CommonHighlighter.delayMillis = HIGHLIGHT_DELAY_MILLIS
    }

    @Test
    fun `Adding a caret cancels the search`() {
        myFixture.configureByText(FILE, "<caret>foo\nbar")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o")
        assertNotNull(ISearchHandler.delegate)

        myFixture.editor.caretModel.addCaret(VisualPosition(1, 0))

        assertNull(ISearchHandler.delegate)
    }

    @Test
    fun `Simple search works`() {
        myFixture.configureByText(FILE, "<caret>foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o")
        myFixture.checkResult("fo<caret>o")
        assertEquals("o", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo")
        assertEquals("", ISearchHandler.delegate?.text)
        assertNull(searchCount)
    }

    @Test
    fun `Empty text search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.searches.clear(SearchType.TEXT)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Empty regexp search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.searches.clear(SearchType.REGEXP)

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Empty reverse search doesn't crash`() {
        myFixture.configureByText(FILE, "foo<caret>")
        ISearchHandler.searches.clear(SearchType.TEXT)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Simple search works 2`() {
        myFixture.configureByText(FILE, "<caret>foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("oo")
        myFixture.checkResult("foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("fo<caret>o")
        assertEquals("o", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Finding second match works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Search - adding letters after finding matches works`() {
        myFixture.configureByText(FILE, "<caret>foop bar foop baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret>p bar foop baz")
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foop bar foo<caret>p baz")
        assertEquals(Pair(2, 2), searchCount)

        type("p")
        myFixture.checkResult("foop bar foop<caret> baz")
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Search can be reversed after failed search`() {
        myFixture.configureByText(FILE, "fool <caret>foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("fool")
        myFixture.checkResult("fool foo<caret>")
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>fool foo")
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)
    }

    @Test
    fun `Reverse search - adding letters after finding matches works`() {
        myFixture.configureByText(FILE, "foop bar foop baz foop<caret> foop")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foop bar foop baz <caret>foop foop")
        assertEquals(Pair(3, 4), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foop bar <caret>foop baz foop foop")
        assertEquals(Pair(2, 4), searchCount)

        type("p")
        myFixture.checkResult("foop bar <caret>foop baz foop foop")
        assertEquals(Pair(2, 4), searchCount)
    }

    @Test
    fun `Reverse search - adding letters after finding matches works 2`() {
        myFixture.configureByText(FILE, "foop bar foop baz foo<caret>p")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foop bar foop baz <caret>foop")
        assertEquals(Pair(3, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foop bar <caret>foop baz foop")
        assertEquals(Pair(2, 3), searchCount)

        type("p")
        myFixture.checkResult("foop bar <caret>foop baz foop")
        assertEquals(Pair(2, 3), searchCount)
    }

    @Test
    fun `Wrap-around search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Wrap-around reverse search works`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo bar <caret>foo")
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Wrap-around search works 2`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |private
                |<caret>bar
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("pri")
        myFixture.checkResult(
            """
                |foo
                |private
                |<caret>bar
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """
                |foo
                |pri<caret>vate
                |bar
            """.trimMargin()
        )

        type("v")
        myFixture.checkResult(
            """
                |foo
                |priv<caret>ate
                |bar
            """.trimMargin()
        )
    }

    @Test
    fun `Using previous search works after finishing with enter`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("oo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
        assertNull(searchCount)
    }

    @Test
    fun `Previous search is not triggered if changing direction with empty search`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("oo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Escape returns to original start`() {
        myFixture.configureByText(FILE, "foo<caret> bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEscape()

        myFixture.checkResult("foo<caret> bar foo")
    }

    @Test
    fun `Isearch text can be edited`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        myFixture.checkResult("foo<caret> fooz foo")

        performEditorAction(ACTION_ISEARCH_EDIT)

        setText("fooz")
        pressPopupEnter()
        myFixture.checkResult("foo fooz<caret> foo")

        assertEquals("fooz", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)
    }

    @Test
    fun `Previous searches can be re-used`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)

        pressPopupEnter()

        myFixture.checkResult("foo foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Text can be added to previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)

        setText("fooz")
        pressPopupEnter()

        myFixture.checkResult("foo fooz<caret> foo")
        assertEquals("fooz", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)
    }

    @Test
    fun `Text can be removed from previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)

        setText("fo")
        pressPopupEnter()

        myFixture.checkResult("foo fo<caret>oz foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), searchCount)
    }

    // This test is disabled b/c in this test, paste doesn't work with a reopened search popup.
    fun `Text can be pasted into previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foobar")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foobar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PASTE)

        performEditorAction(ACTION_ISEARCH_ENTER)
        myFixture.checkResult("foo fooz foobar<caret>")
        assertEquals("foobar", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)
    }

    @Test
    fun `Previous searches can be navigated with previous and next`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foobar fooz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        pressEnter()
        myFixture.checkResult("foo<caret> fooz foobar fooz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("fooz")
        pressEnter()
        myFixture.checkResult("foo fooz<caret> foobar fooz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("bar")
        pressEnter()
        myFixture.checkResult("foo fooz foobar<caret> fooz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_NEXT)
        performEditorAction(ACTION_ISEARCH_FORWARD)

        assertEquals("fooz", ISearchHandler.delegate?.text)
        myFixture.checkResult("foo fooz foobar fooz<caret>")
    }

    @Test
    fun `Previous searches are offered in most recently used order`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("bar")
        assertEquals("bar", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar<caret> baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("baz")
        assertEquals("baz", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar baz<caret> baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)

        pressPopupEnter()
        assertEquals("baz", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar baz baz<caret>")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_ENTER)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD) // Wrap-around
        assertEquals("bar", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar<caret> baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        performEditorAction(ACTION_ISEARCH_ENTER)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD) // Wrap-around
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")
    }

    @Test
    fun `Pressing escape during previous search selection does not save text as previous search`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        type("bar")

        pressEscape()

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current char works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("f", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Search current char works at end of document`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word works several times`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar<caret> foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret>")
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo bar")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word works several times 2`() {
        myFixture.configureByText(FILE, "<caret>foo.bar(foo)bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>.bar(foo)bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar<caret>(foo)bar")
        assertEquals("foo.bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar(foo<caret>)bar")
        assertEquals("foo.bar(foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar(foo)bar<caret>")
        assertEquals("foo.bar(foo)bar", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word works with late second invocation`() {
        myFixture.configureByText(FILE, "<caret>foo.bar foo.bar bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>.bar foo.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo.bar foo<caret>.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar foo.bar<caret> bar")
        assertEquals("foo.bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo.bar foo<caret>.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret>.bar foo.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word shortcut with reverse search works`() {
        myFixture.configureByText(FILE, "foo bar <caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
    }

    @Test
    fun `Search current word with reverse search works several times`() {
        myFixture.configureByText(FILE, "foo bar <caret>foo bar")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo bar")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current word with camel case works as expected`() {
        myFixture.configureByText(FILE, "<caret>fooBarFoo")
        myFixture.editor.settings.isCamelWords = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>BarFoo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("fooBar<caret>Foo")
        assertEquals("fooBar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret>BarFoo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>fooBarFoo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Combination of typed letters and current word search works as expected`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("fo")
        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Combination of typed letters and current word reverse search works as expected`() {
        myFixture.configureByText(FILE, "bar foo bar <caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("ba")
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("ba", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>bar foo bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("ba", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("b", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("bar foo bar <caret>foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search current line works`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar foo
                |baz bar foo
                |baz bar baz
                |foo bar foo
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_LINE)
        myFixture.checkResult(
            """
                |foo bar foo<caret>
                |baz bar foo
                |baz bar baz
                |foo bar foo
            """.trimMargin()
        )
        assertEquals("foo bar foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """
                |foo bar foo
                |baz bar foo
                |baz bar baz
                |foo bar foo<caret>
            """.trimMargin()
        )
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Backspace works as expected`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("oo")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("o", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Backspace works as expected after failed search`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("bar")
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)

        type("rab")
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barrab", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barra", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barr", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo ba<caret>r foo")
        assertEquals("ba", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)
    }

    @Test
    fun `Changing direction works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("oo")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar f<caret>oo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 3), searchCount)
    }

    @Test
    fun `Changing direction works 2`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("oo")
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Regexp search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Regexp backward search works`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        type("o{2}")
        myFixture.checkResult("foo bar f<caret>oo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Changing direction in regexp search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.checkResult("foo bar f<caret>oo")
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Pasting from clipboard works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PASTE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Pasting from clipboard works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        CopyPasteManager.getInstance().setContents(StringSelection("ar"))

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("b")
        myFixture.checkResult("foo b<caret>ar foo")
        assertEquals("b", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_PASTE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo b<caret>ar foo")
        assertEquals("b", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Paste history replaces the pasted text with the next clipboard item`() {
        resetClipboard("older", "bar")
        myFixture.configureByText(FILE, "<caret>foo bar older")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PASTE)
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)

        assertEquals("older", ISearchHandler.delegate?.text)
        myFixture.checkResult("foo bar older<caret>")
    }

    @Test
    fun `Paste history keeps whatever was typed before the paste`() {
        resetClipboard("older", "bar")
        myFixture.configureByText(FILE, "<caret>foo bar foo older")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo ")
        performEditorAction(ACTION_ISEARCH_PASTE)
        assertEquals("foo bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)

        assertEquals("foo older", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Paste history cycles back round to the first item`() {
        resetClipboard("older", "bar")
        myFixture.configureByText(FILE, "<caret>foo bar older")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PASTE)
        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)
        assertEquals("older", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)

        assertEquals("bar", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Paste history does nothing when the previous action was not a paste`() {
        resetClipboard("older", "bar")
        myFixture.configureByText(FILE, "<caret>foo bar older")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PASTE)
        type("z")

        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)

        assertEquals("barz", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Failing search marks only the characters added since the last match`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        assertEquals("foo", markup())

        type("b")

        assertEquals(markupOf(found = "foo", notFound = "b"), markup())
        assertEquals("foob", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Further failing characters extend the marked part`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        type("b")

        type("z")

        assertEquals(markupOf(found = "foo", notFound = "bz"), markup())
    }

    @Test
    fun `Backspacing back to a matching search clears the marking`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        type("bz")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        assertEquals(markupOf(found = "foo", notFound = "b"), markup())

        performEditorAction(ACTION_ISEARCH_BACKSPACE)

        assertEquals("foo", markup())
    }

    @Test
    fun `Marked search text is escaped`() {
        myFixture.configureByText(FILE, "<caret>a<b & c")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("a<")

        type("Z")

        assertEquals(markupOf(found = "a&lt;", notFound = "Z"), markup())
        assertEquals("a<Z", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Text iSearch key binding works during regexp search`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Breadcrumb works as expected when changing direction`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("f", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Breadcrumb works as expected when changing direction 2`() {
        myFixture.configureByText(FILE, "<caret>foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Simple reverse search works`() {
        myFixture.configureByText(FILE, "foo foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Simple reverse search works 2`() {
        myFixture.configureByText(FILE, "foo foo<caret> ")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo ")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search starts at prompt`() {
        myFixture.configureByText(FILE, "foo<caret>foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("f")
        myFixture.checkResult("foof<caret>oo")
        type("o")
        myFixture.checkResult("foofo<caret>o")
        type("o")
        myFixture.checkResult("foofoo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Reverse search starts at prompt`() {
        myFixture.configureByText(FILE, "foo<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("f")
        myFixture.checkResult("<caret>foofoo")
        type("o")
        myFixture.checkResult("<caret>foofoo")
        type("o")
        myFixture.checkResult("<caret>foofoo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Change direction happens at prompt`() {
        myFixture.configureByText(FILE, "<caret>foofoo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foofoo")

        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Simple reverse search works when not at end of document`() {
        myFixture.configureByText(FILE, "foo<caret> bar")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("oo")
        myFixture.checkResult("f<caret>oo bar")
        assertEquals("oo", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Search with new line works`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo
                |foo
                |bar
                |baz
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        performEditorAction(ACTION_ISEARCH_NEWLINE)
        type("bar")

        myFixture.checkResult(
            """
                |foo
                |foo
                |bar<caret>
                |baz
            """.trimMargin()
        )
    }

    @Test
    fun `Multiple reverse searches work`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("f")
        myFixture.checkResult("foo bar <caret>foo")
        type("o")
        myFixture.checkResult("foo bar <caret>foo")
        type("o")
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Multiple caret search works over same texts`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar foo
                |<caret>foo bar baz
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        myFixture.checkResult(
            """
                |foo<caret> bar foo
                |foo<caret> bar baz
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult(
            """
                |foo bar<caret> foo
                |foo bar<caret> baz
            """.trimMargin()
        )
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 2), searchCount)
    }

    @Test
    fun `Multiple caret search works over different texts`() {
        myFixture.configureByText(
            FILE,
            """
                |(<caret>foo bar) baz
                |(<caret>foo baz) bar
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type(")")

        myFixture.checkResult(
            """
                |(foo bar)<caret> baz
                |(foo baz)<caret> bar
            """.trimMargin()
        )
        assertEquals(")", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 2), searchCount)
    }

    @Test
    fun `Multiple caret search can be reversed after failed search`() {
        myFixture.configureByText(
            FILE,
            """
                |fool (<caret>foo bar) baz
                |(<caret>foo baz) bar
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("fool")

        myFixture.checkResult(
            """
                |fool (foo<caret> bar) baz
                |(foo<caret> baz) bar
            """.trimMargin()
        )
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)

        myFixture.checkResult(
            """
                |<caret>fool (foo bar) baz
                |(foo baz) bar
            """.trimMargin()
        )
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), searchCount)
    }

    @Test
    fun `Multiple caret search works over overlapping texts`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar baz
                |<caret>foo bar bax
                |foo bar bay
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        myFixture.checkResult(
            """
                |foo<caret> bar baz
                |foo<caret> bar bax
                |foo bar bay
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """
                |foo bar baz
                |foo<caret> bar bax
                |foo<caret> bar bay
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |foo<caret> bar baz
                |foo<caret> bar bax
                |foo bar bay
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)
    }

    @Test
    fun `Reverse multiple caret search works over overlapping texts`() {
        myFixture.configureByText(
            FILE,
            """
                |foo bar baz
                |foo bar bax<caret>
                |foo bar<caret> bay
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")

        myFixture.checkResult(
            """
                |foo bar baz
                |<caret>foo bar bax
                |<caret>foo bar bay
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult(
            """
                |<caret>foo bar baz
                |<caret>foo bar bax
                |foo bar bay
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |foo bar baz
                |<caret>foo bar bax
                |<caret>foo bar bay
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)
    }

    @Test
    fun `Mark is set when search starts`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar baz
                |foo bar bax
                |foo bar bay
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """
                |foo bar baz
                |foo bar bax
                |foo<caret> bar bay
            """.trimMargin()
        )

        performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                |<caret>foo bar baz
                |foo bar bax
                |foo bar bay
            """.trimMargin()
        )
    }

    @Test
    fun `Swap at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    @Test
    fun `Swap at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    @Test
    fun `Swap at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("foo foo<caret> bar baz")
    }

    @Test
    fun `Mark at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo</selection><caret> foo bar baz")
    }

    @Test
    fun `Mark at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo<caret></selection> foo bar baz")
    }

    @Test
    fun `Mark at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("foo <selection><caret>foo</selection> bar baz")
    }

    @Test
    fun `Isearch with lax search works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o e")
        myFixture.checkResult("foo bar ye<caret>s sir")

        ISearchHandler.isLax = false
    }

    @Test
    fun `Isearch with lax search works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o e i")
        myFixture.checkResult("foo bar yes si<caret>r")

        ISearchHandler.isLax = false
    }

    @Test
    fun `Search can be toggled from lax to non-lax`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo bar")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("f r")
        myFixture.checkResult("foo bar<caret> foo bar foo bar")
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")

        performEditorAction(ACTION_TOGGLE_LAX_SEARCH)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")

        ISearchHandler.isLax = false
    }

    @Test
    fun `Search can be toggled from non-lax to lax`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo bar")
        ISearchHandler.isLax = false

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("f r")
        myFixture.checkResult("f<caret>oo bar foo bar foo bar")

        performEditorAction(ACTION_TOGGLE_LAX_SEARCH)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")
    }

    @Test
    fun `Search can be toggled from undefined case to case sensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> Foo foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> bar")
    }

    @Test
    fun `Search can be toggled from case sensitive to case insensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> Foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo Foo<caret> bar")
    }

    @Test
    fun `Search can be toggled from undefined case to case insensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("Foo")
        myFixture.checkResult("foo Foo<caret> foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> Foo bar")
    }

    @Test
    fun `Search can be toggled from case insensitive to case sensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("Foo")
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo foo Foo<caret> bar")
    }

    @Test
    fun `Case sensitivity state is remembered in breadcrumbs`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("Foo")
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo foo Foo<caret> bar")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")
    }

    @Test
    fun `Search can be toggled from text to regexp`() {
        myFixture.configureByText(FILE, "<caret>foo foo+ foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo+")
        myFixture.checkResult("foo foo+<caret> foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo+<caret> foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_REGEXP)
        assertEquals(SearchType.REGEXP, ISearchHandler.delegate?.searchType)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo+ foo<caret> bar")
    }

    @Test
    fun `Search can be toggled from regexp to text`() {
        myFixture.configureByText(FILE, "<caret>foo fo[o] bar")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("fo[o]")
        myFixture.checkResult("foo<caret> fo[o] bar")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("foo<caret> fo[o] bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_REGEXP)
        assertEquals(SearchType.TEXT, ISearchHandler.delegate?.searchType)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo fo[o]<caret> bar")
    }

    @Test
    fun `Search type is remembered in breadcrumbs`() {
        myFixture.configureByText(FILE, "<caret>foo foo+ foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo+")
        myFixture.checkResult("foo foo+<caret> foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_REGEXP)
        assertEquals(SearchType.REGEXP, ISearchHandler.delegate?.searchType)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo+ foo<caret> bar")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        assertEquals(SearchType.REGEXP, ISearchHandler.delegate?.searchType)
        myFixture.checkResult("foo foo+<caret> foo bar")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        assertEquals(SearchType.TEXT, ISearchHandler.delegate?.searchType)
        myFixture.checkResult("foo<caret> foo+ foo bar")
    }

    @Test
    fun `First match can be reached when searching forward`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), searchCount)
    }

    @Test
    fun `First match when already on first match is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)
    }

    @Test
    fun `First match when no match is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("purport")
        myFixture.checkResult(
            """
                |import
                |import
                |imp<caret>ort
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |import
                |import
                |imp<caret>ort
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)
    }

    @Test
    fun `First match when search has failed is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)
    }

    @Test
    fun `First match can be reached when searching backward`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), searchCount)
    }

    @Test
    fun `First match when already on first match is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)
    }

    @Test
    fun `First match when no match is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("purport")
        myFixture.checkResult(
            """
                |im<caret>port
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |im<caret>port
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)
    }

    @Test
    fun `First match when search has failed is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), searchCount)
    }

    @Test
    fun `Last match can be reached when searching backward`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), searchCount)
    }

    @Test
    fun `Last match when already on last match is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import
                |<caret>import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)
    }

    @Test
    fun `Last match when no match is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("purport")
        myFixture.checkResult(
            """
                |import
                |im<caret>port
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |im<caret>port
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)
    }

    @Test
    fun `Last match when search has failed is handled in backward search`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("import")
        myFixture.checkResult(
            """
                |<caret>import
                |import
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)
    }

    @Test
    fun `Last match can be reached when searching forward`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |<caret>import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult(
            """
                |import
                |import<caret>
                |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), searchCount)
    }

    @Test
    fun `Last match when already on last match is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import
                |<caret>import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)
    }

    @Test
    fun `Last match when no match is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import<caret>
                |import
                |import
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("purport")
        myFixture.checkResult(
            """
                |import
                |imp<caret>ort
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |imp<caret>ort
                |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), searchCount)
    }

    @Test
    fun `Last match when search has failed is handled in forward search`() {
        myFixture.configureByText(
            FILE,
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("import")
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), searchCount)

        performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
                |import
                |import
                |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), searchCount)
    }

    @Test
    fun `Forward selection search works`() {
        myFixture.configureByText(FILE, "<selection>foo<caret></selection> bar foo")
        ISearchHandler.isSelectionISearch = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Forward selection search works 2`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection> bar foo")
        ISearchHandler.isSelectionISearch = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Backward selection search works`() {
        myFixture.configureByText(FILE, "foo bar <selection><caret>foo</selection>")
        ISearchHandler.isSelectionISearch = true

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Backward selection search works 2`() {
        myFixture.configureByText(FILE, "foo bar <selection>foo<caret></selection>")
        ISearchHandler.isSelectionISearch = true

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Delete char in forward search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar fo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        myFixture.checkResult("foo<caret> bar fo")
        assertEquals(Pair(1, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_DELETE_CHAR)
        myFixture.checkResult("fo<caret>o bar fo")
        assertEquals(Pair(1, 2), searchCount)
    }

    @Test
    fun `Delete char in backward search works`() {
        myFixture.configureByText(FILE, "fo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")

        myFixture.checkResult("fo bar <caret>foo")
        assertEquals(Pair(1, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_DELETE_CHAR)
        myFixture.checkResult("fo bar <caret>foo")
        assertEquals(Pair(2, 2), searchCount)
    }

    @Test
    fun `Search is disabled in an editor without a project`() {
        val factory = EditorFactory.getInstance()
        val editor = factory.createEditor(factory.createDocument("foo bar baz"))
        try {
            assertNull(editor.project)
            val handler = ISearchHandler(direction = SearchDirection.FORWARD, type = SearchType.TEXT)

            assertFalse(handler.isEnabled(editor, editor.caretModel.primaryCaret, DataContext.EMPTY_CONTEXT))

            // FindManager is a project service, so starting a search here would fail on the first typed character.
            handler.execute(editor, null, DataContext.EMPTY_CONTEXT)
            assertNull(ISearchHandler.delegate)
        } finally {
            factory.releaseEditor(editor)
        }
    }

    @Test
    fun `Changing search type starts the walk through previous searches again`() {
        myFixture.configureByText(FILE, "<caret>aaa bbb xxx yyy")
        ISearchHandler.searches.clear(SearchType.TEXT)
        ISearchHandler.searches.clear(SearchType.REGEXP)

        [
            ACTION_ISEARCH_FORWARD to "aaa",
            ACTION_ISEARCH_FORWARD to "bbb",
            ACTION_ISEARCH_REGEXP_FORWARD to "xxx",
            ACTION_ISEARCH_REGEXP_FORWARD to "yyy"
        ].forEach { (action, searched) ->
            performEditorAction(action)
            type(searched)
            pressEnter()
        }

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("bbb", ISearchHandler.delegate?.text)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("aaa", ISearchHandler.delegate?.text)

        // Two steps into the text history, switching to regexp offers the most recent regexp search. Carrying the
        // position across would land two steps into the regexp history instead.
        performEditorAction(ACTION_ISEARCH_TOGGLE_REGEXP)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("yyy", ISearchHandler.delegate?.text)

        // Switching back does the same in the other direction rather than resuming where the text walk stopped.
        performEditorAction(ACTION_ISEARCH_TOGGLE_REGEXP)
        performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("bbb", ISearchHandler.delegate?.text)
    }

    @Test
    fun `Half-typed regexp does not break a backward search`() {
        // ISearchDelegate.matchEnd() compiles the search string itself, but only on the backward first-search path, and only once a
        // match has moved the caret away from where the search started.
        myFixture.configureByText(FILE, "foo bar <caret>baz")

        performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        type("b")
        myFixture.checkResult("foo <caret>bar baz")

        ["(", "[", "\\"].forEach { unfinished ->
            type(unfinished)
            assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)
            performEditorAction(ACTION_ISEARCH_BACKSPACE)
        }

        // Completing the pattern picks the search up again where the half-typed one left it.
        type("(a)")
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)
        myFixture.checkResult("foo <caret>bar baz")
    }

    @Test
    fun `Editing the search string keeps the case sensitivity of the search`() {
        myFixture.configureByText(FILE, "<caret>foo FOO foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(2, secondaryHighlightCount())

        // Editing highlights through the same rules as searching, so the case sensitivity just asked for still holds.
        performEditorAction(ACTION_ISEARCH_EDIT)
        pressKeyAndSettle()
        assertEquals(2, secondaryHighlightCount())
    }

    @Test
    fun `Editing the search string keeps lax whitespace matching`() {
        myFixture.configureByText(FILE, "<caret>foo  bar and foo bar")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo bar")
        assertEquals(2, secondaryHighlightCount())

        performEditorAction(ACTION_ISEARCH_EDIT)
        pressKeyAndSettle()
        assertEquals(2, secondaryHighlightCount())

        ISearchHandler.isLax = false
    }

    @Test
    fun `Repeating a search keeps the highlights of every match`() {
        myFixture.configureByText(FILE, "<caret>foo foo foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        assertEquals(3, secondaryHighlightCount())

        // Repeating leaves the search string alone, so the whole-file highlights are kept rather than cleared and
        // recomputed -- only the marker on the current match moves.
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo<caret> foo")
        assertEquals(3, secondaryHighlightCount())
    }

    @Test
    fun `Paste history does nothing after a character has been deleted`() {
        resetClipboard("older", "bar")
        myFixture.configureByText(FILE, "<caret>foo bar older")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_PASTE)
        assertEquals("bar", ISearchHandler.delegate?.text)

        performEditorAction(ACTION_ISEARCH_DELETE_CHAR)
        assertEquals("ba", ISearchHandler.delegate?.text)

        // Deleting ended the walk, so there is no longer a pasted tail to swap for the next clipboard entry.
        performEditorAction(ACTION_ISEARCH_PASTE_HISTORY)
        assertEquals("ba", ISearchHandler.delegate?.text)
    }

    @Test
    fun `A search leaves highlights belonging to the rest of the IDE alone`() {
        myFixture.configureByText(FILE, "foo bar <caret>baz")

        // Stands in for a highlight-usages mark, a console hyperlink or a diff highlight.
        val foreign = myFixture.editor.markupModel.addRangeHighlighter(
            EditorColors.SEARCH_RESULT_ATTRIBUTES,
            0,
            3,
            HighlighterLayer.LAST,
            HighlighterTargetArea.EXACT_RANGE
        )

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("bar")
        assertTrue(foreign.isValid, "cleared while searching")

        pressEnter()

        assertTrue(foreign.isValid, "cleared when the search ended")
        // The same object, not a copy: whoever painted it still holds this reference and must be able to remove it.
        assertTrue(myFixture.editor.markupModel.allHighlighters.contains(foreign))
        // The session still cleans up after itself rather than leaking its own highlights.
        assertEquals(0, secondaryHighlightCount())
    }

    /**
     * Search actions settle before returning. Highlighting is debounced, and a breadcrumb records the match count
     * as it stands when the next action starts, so firing actions faster than the debounce would snapshot counts
     * that have not arrived yet -- something a user typing at the keyboard never does.
     */
    @Test
    fun `Isearch keystroke handlers step aside while the search string is edited`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        val caret = myFixture.editor.caretModel.primaryCaret
        val backspace = ISearchBackspaceHandler()
        val paste = ISearchPasteHandler()
        val pasteHistory = ISearchPasteHistoryHandler()

        assertTrue(backspace.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))
        assertTrue(paste.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))
        assertTrue(pasteHistory.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))

        performEditorAction(ACTION_ISEARCH_EDIT)

        // Disabled, so the keystroke reaches the popup's own editor and acts at its caret instead of the string end.
        assertFalse(backspace.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))
        assertFalse(paste.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))
        assertFalse(pasteHistory.isEnabled(myFixture.editor, caret, DataContext.EMPTY_CONTEXT))
    }

    @Test
    fun `Typing no longer appends to the search string once it is being edited`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        performEditorAction(ACTION_ISEARCH_EDIT)

        myFixture.type('x')

        // The delegate keeps its hands off: what happens to the character is the popup editor's business.
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    private fun performEditorAction(actionId: String) {
        myFixture.performEditorAction(actionId)
        waitForHighlighting()
    }

    private fun type(text: String) {
        // One character at a time: each keystroke starts a search, and a breadcrumb records the count as it stands
        // when the next one starts. Typing a whole string inside one debounce window is faster than a user can type.
        text.forEach {
            myFixture.type(it)
            waitForHighlighting()
        }
    }

    /**
     * Waits for debounced highlighting to finish. Searching reports its match count from a pooled thread back onto
     * the EDT, so the count is not yet up to date when a search action returns.
     */
    private fun waitForHighlighting() {
        PlatformTestUtil.waitWithEventsDispatching(
            "Highlighting did not finish",
            { CommonHighlighter.isIdle },
            HIGHLIGHT_TIMEOUT_SECONDS
        )
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    private fun pressEnter() {
        performEditorAction(ACTION_ISEARCH_ENTER)
    }

    /** Clipboard history is application-scoped, so it has to be pinned for a test that walks through it. */
    private fun resetClipboard(vararg items: String) {
        val manager = CopyPasteManagerEx.getInstanceEx()
        manager.allContents.forEach { manager.removeContent(it) }
        items.forEach { manager.setContents(StringSelection(it)) }
    }

    private fun markup() = ISearchHandler.delegate?.ui?.markup

    private fun markupOf(found: String, notFound: String) =
        """<html>$found<font color="${ColorUtil.toHtmlColor(JBColor.RED)}">$notFound</font></html>"""

    private fun pressEscape() {
        pressKey(ISearchHandler.delegate?.ui, VK_ESCAPE)
        ISearchHandler.delegate?.hide()
    }

    private fun pressPopupEnter() {
        val textField = ISearchHandler.delegate!!.ui.textField
        val popup = ISearchHandler.delegate!!.ui.popup
        popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
        popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
    }

    /** Secondary highlights are the ones the debounced whole-file search paints; primary marks the current match. */
    private fun secondaryHighlightCount(): Int =
        myFixture.editor.markupModel.allHighlighters.count { it.textAttributesKey == EMACSJ_SECONDARY }

    /** Any key release other than Enter is what makes an edited search string re-highlight. */
    private fun pressKeyAndSettle() {
        pressKey(ISearchHandler.delegate?.ui, VK_SHIFT)
        waitForHighlighting()
    }

    private fun setText(newText: String) {
        ISearchHandler.delegate!!.ui.textField.text = newText
    }
}
