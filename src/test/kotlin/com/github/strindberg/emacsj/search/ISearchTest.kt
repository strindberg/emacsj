package com.github.strindberg.emacsj.search

import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.PlatformTestUtil

private const val FILE = "isearchfile.txt"

private const val HIGHLIGHT_TIMEOUT_SECONDS = 10

@Suppress("LargeClass", "ReplaceSafeCallChainWithRun")
class ISearchTest : EmacsJTestCase() {

    override fun setUp() {
        super.setUp()
        CommonHighlighter.delayMillis = 0
    }

    override fun tearDown() {
        try {
            CommonHighlighter.delayMillis = HIGHLIGHT_DELAY_MILLIS
        } finally {
            super.tearDown()
        }
    }

    /** Match count as shown in the search UI, once debounced highlighting has reported it. */
    private val searchCount: Pair<Int, Int>?
        get() {
            waitForHighlighting()
            return ISearchHandler.delegate?.ui?.count
        }

    fun `test Simple search works`() {
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

    fun `test Empty text search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.lastStringSearches = emptyList()

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Empty regexp search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.lastRegexpSearches = emptyList()

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Empty reverse search doesn't crash`() {
        myFixture.configureByText(FILE, "foo<caret>")
        ISearchHandler.lastStringSearches = emptyList()

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKSPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Simple search works 2`() {
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

    fun `test Finding second match works`() {
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

    fun `test Search - adding letters after finding matches works`() {
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

    fun `test Search can be reversed after failed search`() {
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

    fun `test Reverse search - adding letters after finding matches works`() {
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

    fun `test Reverse search - adding letters after finding matches works 2`() {
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

    fun `test Wrap-around search works`() {
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

    fun `test Wrap-around reverse search works`() {
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

    fun `test Wrap-around search works 2`() {
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

    fun `test Using previous search works after finishing with enter`() {
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

    fun `test Previous search is not triggered if changing direction with empty search`() {
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

    fun `test Escape returns to original start`() {
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

    fun `test Isearch text can be edited`() {
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

    fun `test Previous searches can be re-used`() {
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

    fun `test Text can be added to previous search`() {
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

    fun `test Text can be removed from previous search`() {
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

    fun `test Previous searches can be navigated with previous and next`() {
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

    fun `test Previous searches are offered in most recently used order`() {
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

    fun `test Pressing escape during previous search selection does not save text as previous search`() {
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

    fun `test Search current char works`() {
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

    fun `test Search current char works at end of document`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current word works`() {
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

    fun `test Search current word works several times`() {
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

    fun `test Search current word works several times 2`() {
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

    fun `test Search current word works with late second invocation`() {
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

    fun `test Search current word shortcut with reverse search works`() {
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

    fun `test Search current word with reverse search works several times`() {
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

    fun `test Search current word with camel case works as expected`() {
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

    fun `test Combination of typed letters and current word search works as expected`() {
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

    fun `test Combination of typed letters and current word reverse search works as expected`() {
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

    fun `test Search current line works`() {
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

    fun `test Backspace works as expected`() {
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

    fun `test Backspace works as expected after failed search`() {
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

    fun `test Changing direction works 1`() {
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

    fun `test Changing direction works 2`() {
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

    fun `test Regexp search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    fun `test Regexp backward search works`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        type("o{2}")
        myFixture.checkResult("foo bar f<caret>oo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    fun `test Changing direction in regexp search works`() {
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

    fun `test Pasting from clipboard works`() {
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

    fun `test Pasting with plugin paste works`() {
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

    fun `test Pasting from clipboard works 2`() {
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

    fun `test Text iSearch key binding works during regexp search`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
    }

    fun `test Breadcrumb works as expected when changing direction`() {
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

    fun `test Breadcrumb works as expected when changing direction 2`() {
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

    fun `test Simple reverse search works`() {
        myFixture.configureByText(FILE, "foo foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), searchCount)
    }

    fun `test Simple reverse search works 2`() {
        myFixture.configureByText(FILE, "foo foo<caret> ")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo ")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    fun `test Search starts at prompt`() {
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

    fun `test Reverse search starts at prompt`() {
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

    fun `test Change direction happens at prompt`() {
        myFixture.configureByText(FILE, "<caret>foofoo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret>foo")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foofoo")

        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), searchCount)
    }

    fun `test Simple reverse search works when not at end of document`() {
        myFixture.configureByText(FILE, "foo<caret> bar")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("oo")
        myFixture.checkResult("f<caret>oo bar")
        assertEquals("oo", ISearchHandler.delegate?.text)
    }

    fun `test Search with new line works`() {
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

    fun `test Multiple reverse searches work`() {
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

    fun `test Multiple caret search works over same texts`() {
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

    fun `test Multiple caret search works over different texts`() {
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

    fun `test Multiple caret search can be reversed after failed search`() {
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

    fun `test Multiple caret search works over overlapping texts`() {
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

    fun `test Reverse multiple caret search works over overlapping texts`() {
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

    fun `test Mark is set when search starts`() {
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

    fun `test Swap at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    fun `test Swap at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    fun `test Swap at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("foo foo<caret> bar baz")
    }

    fun `test Mark at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo</selection><caret> foo bar baz")
    }

    fun `test Mark at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo<caret></selection> foo bar baz")
    }

    fun `test Mark at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("foo <selection><caret>foo</selection> bar baz")
    }

    fun `test Isearch with lax search works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o e")
        myFixture.checkResult("foo bar ye<caret>s sir")
    }

    fun `test Isearch with lax search works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.isLax = true

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("o e i")
        myFixture.checkResult("foo bar yes si<caret>r")
    }

    fun `test Search can be toggled from lax to non-lax`() {
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
    }

    fun `test Search can be toggled from non-lax to lax`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo bar")
        ISearchHandler.isLax = false

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("f r")
        myFixture.checkResult("f<caret>oo bar foo bar foo bar")

        performEditorAction(ACTION_TOGGLE_LAX_SEARCH)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")
    }

    fun `test Search can be toggled from undefined case to case sensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")
        myFixture.checkResult("foo<caret> Foo foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> bar")
    }

    fun `test Search can be toggled from case sensitive to case insensitive`() {
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

    fun `test Search can be toggled from undefined case to case insensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo Foo bar")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("Foo")
        myFixture.checkResult("foo Foo<caret> foo Foo bar")

        performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> Foo bar")
    }

    fun `test Search can be toggled from case insensitive to case sensitive`() {
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

    fun `test Case sensitivity state is remembered in breadcrumbs`() {
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

    fun `test Search can be toggled from text to regexp`() {
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

    fun `test Search can be toggled from regexp to text`() {
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

    fun `test Search type is remembered in breadcrumbs`() {
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

    fun `test First match can be reached when searching forward`() {
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

    fun `test First match when already on first match is handled in forward search`() {
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

    fun `test First match when no match is handled in forward search`() {
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

    fun `test First match when search has failed is handled in forward search`() {
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

    fun `test First match can be reached when searching backward`() {
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

    fun `test First match when already on first match is handled in backward search`() {
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

    fun `test First match when no match is handled in backward search`() {
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

    fun `test First match when search has failed is handled in backward search`() {
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

    fun `test Last match can be reached when searching backward`() {
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

    fun `test Last match when already on last match is handled in backward search`() {
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

    fun `test Last match when no match is handled in backward search`() {
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

    fun `test Last match when search has failed is handled in backward search`() {
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

    fun `test Last match can be reached when searching forward`() {
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

    fun `test Last match when already on last match is handled in forward search`() {
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

    fun `test Last match when no match is handled in forward search`() {
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

    fun `test Last match when search has failed is handled in forward search`() {
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

    fun `test Forward selection search works`() {
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

    fun `test Forward selection search works 2`() {
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

    fun `test Backward selection search works`() {
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

    fun `test Backward selection search works 2`() {
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

    fun `test Delete char in forward search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar fo")

        performEditorAction(ACTION_ISEARCH_FORWARD)
        type("foo")

        myFixture.checkResult("foo<caret> bar fo")
        assertEquals(Pair(1, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_DELETE_CHAR)
        myFixture.checkResult("fo<caret>o bar fo")
        assertEquals(Pair(1, 2), searchCount)
    }

    fun `test Delete char in backward search works`() {
        myFixture.configureByText(FILE, "fo bar foo<caret>")

        performEditorAction(ACTION_ISEARCH_BACKWARD)
        type("foo")

        myFixture.checkResult("fo bar <caret>foo")
        assertEquals(Pair(1, 1), searchCount)

        performEditorAction(ACTION_ISEARCH_DELETE_CHAR)
        myFixture.checkResult("fo bar <caret>foo")
        assertEquals(Pair(2, 2), searchCount)
    }

    private fun pressEnter() {
        performEditorAction(ACTION_ISEARCH_ENTER)
        ISearchHandler.delegate?.hide()
    }

    /**
     * Search actions settle before returning. Highlighting is debounced, and a breadcrumb records the match count
     * as it stands when the next action starts, so firing actions faster than the debounce would snapshot counts
     * that have not arrived yet -- something a user typing at the keyboard never does.
     */
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

    private fun pressEscape() {
        val popup = ISearchHandler.delegate?.ui?.popup
        val textField = ISearchHandler.delegate?.ui?.textField
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        popup?.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ESCAPE, CHAR_UNDEFINED))
        ISearchHandler.delegate?.hide()
    }

    private fun pressPopupEnter() {
        val textField = ISearchHandler.delegate!!.ui.textField
        val popup = ISearchHandler.delegate!!.ui.popup
        popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
        popup.dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
    }

    private fun setText(newText: String) {
        ISearchHandler.delegate!!.ui.textField.text = newText
    }
}
