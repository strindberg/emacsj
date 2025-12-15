package com.github.strindberg.emacsj.search

import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ENTER
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class ISearchTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        CommonHighlighter.testing = true
    }

    override fun tearDown() {
        ISearchHandler.delegate?.hide()
        super.tearDown()
    }

    fun `test Simple search works`() {
        myFixture.configureByText(FILE, "<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("o")
        myFixture.checkResult("fo<caret>o")
        assertEquals("o", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo")
        assertEquals("", ISearchHandler.delegate?.text)
        assertNull(ISearchHandler.delegate?.ui?.count)
    }

    fun `test Empty text search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.lastStringSearches = listOf()

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("<caret>foo")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Empty regexp search doesn't crash`() {
        myFixture.configureByText(FILE, "<caret>foo")
        ISearchHandler.lastRegexpSearches = listOf()

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("<caret>foo")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Empty reverse search doesn't crash`() {
        myFixture.configureByText(FILE, "foo<caret>")
        ISearchHandler.lastStringSearches = listOf()

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo<caret>")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Simple search works 2`() {
        myFixture.configureByText(FILE, "<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("oo")
        myFixture.checkResult("foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("fo<caret>o")
        assertEquals("o", ISearchHandler.delegate?.text)
    }

    fun `test Finding second match works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Search - adding letters after finding matches works`() {
        myFixture.configureByText(FILE, "<caret>foop bar foop baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret>p bar foop baz")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foop bar foo<caret>p baz")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.type("p")
        myFixture.checkResult("foop bar foop<caret> baz")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Search can be reversed after failed search`() {
        myFixture.configureByText(FILE, "fool <caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("fool")
        myFixture.checkResult("fool foo<caret>")
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 1), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>fool foo")
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Reverse search - adding letters after finding matches works`() {
        myFixture.configureByText(FILE, "foop bar foop baz foop<caret> foop")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foop bar foop baz <caret>foop foop")
        assertEquals(Pair(3, 4), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foop bar <caret>foop baz foop foop")
        assertEquals(Pair(2, 4), ISearchHandler.delegate?.ui?.count)

        myFixture.type("p")
        myFixture.checkResult("foop bar <caret>foop baz foop foop")
        assertEquals(Pair(2, 4), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Reverse search - adding letters after finding matches works 2`() {
        myFixture.configureByText(FILE, "foop bar foop baz foo<caret>p")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foop bar foop baz <caret>foop")
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foop bar <caret>foop baz foop")
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.type("p")
        myFixture.checkResult("foop bar <caret>foop baz foop")
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Wrap-around search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Wrap-around reverse search works`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo bar <caret>foo")
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Wrap-around search works 2`() {
        myFixture.configureByText(
            FILE,
            """foo
            |private
            |<caret>bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("pri")
        myFixture.checkResult(
            """foo
            |private
            |<caret>bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """foo
            |pri<caret>vate
            |bar
            """.trimMargin()
        )

        myFixture.type("v")
        myFixture.checkResult(
            """foo
            |priv<caret>ate
            |bar
            """.trimMargin()
        )
    }

    fun `test Using previous search works after finishing with enter`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("oo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
        assertNull(ISearchHandler.delegate?.ui?.count)
    }

    fun `test Using previous search works after finishing with escape`() {
        myFixture.configureByText(FILE, "<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("o")

        pressEscape()

        myFixture.checkResult("<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("fo<caret>o")
        assertEquals("o", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo")
        assertEquals("", ISearchHandler.delegate?.text)
        assertNull(ISearchHandler.delegate?.ui?.count)
    }

    fun `test Previous search is not triggered if changing direction with empty search`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("oo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Escape returns to original start`() {
        myFixture.configureByText(FILE, "foo<caret> bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEscape()

        myFixture.checkResult("foo<caret> bar foo")
    }

    fun `test Isearch text can be edited`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        myFixture.performEditorAction(ACTION_EDITOR_ENTER)

        myFixture.checkResult("foo<caret> fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_EDIT)

        setText("fooz")
        pressPopupEnter()
        myFixture.checkResult("foo fooz<caret> foo")

        assertEquals("fooz", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Previous searches can be re-used`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)

        pressPopupEnter()

        myFixture.checkResult("foo foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Text can be added to previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)

        setText("fooz")
        pressPopupEnter()

        myFixture.checkResult("foo fooz<caret> foo")
        assertEquals("fooz", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Text can be removed from previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)

        setText("fo")
        pressPopupEnter()

        myFixture.checkResult("foo fo<caret>oz foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
    }

    // This test is disabled b/c in this test, paste doesn't work with a reopened search popup.
    fun `Text can be pasted into previous search`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foobar")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> fooz foobar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_EDITOR_PASTE)

        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
        myFixture.checkResult("foo fooz foobar<caret>")
        assertEquals("foobar", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Previous searches can be navigated with previous and next`() {
        myFixture.configureByText(FILE, "<caret>foo fooz foobar fooz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        pressEnter()
        myFixture.checkResult("foo<caret> fooz foobar fooz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("fooz")
        pressEnter()
        myFixture.checkResult("foo fooz<caret> foobar fooz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("bar")
        pressEnter()
        myFixture.checkResult("foo fooz foobar<caret> fooz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_NEXT)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)

        assertEquals("fooz", ISearchHandler.delegate?.text)
        myFixture.checkResult("foo fooz foobar fooz<caret>")
    }

    fun `test Previous searches are offered in most recently used order`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("bar")
        assertEquals("bar", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar<caret> baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("baz")
        assertEquals("baz", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar baz<caret> baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)

        pressPopupEnter()
        assertEquals("baz", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar baz baz<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD) // Wrap-around
        assertEquals("bar", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo bar<caret> baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD) // Wrap-around
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")
    }

    fun `test Pressing escape during previous search selection does not save text as previous search`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        pressEnter()

        myFixture.checkResult("foo<caret> bar baz baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.type("bar")

        pressEscape()

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    fun `test Search current char works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("f", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Search current char works at end of document`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_CHAR)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current word works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current word works several times`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar<caret> foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret>")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo bar")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current word works several times 2`() {
        myFixture.configureByText(FILE, "<caret>foo.bar(foo)bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>.bar(foo)bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar<caret>(foo)bar")
        assertEquals("foo.bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar(foo<caret>)bar")
        assertEquals("foo.bar(foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar(foo)bar<caret>")
        assertEquals("foo.bar(foo)bar", ISearchHandler.delegate?.text)
    }

    fun `test Search current word works with late second invocation`() {
        myFixture.configureByText(FILE, "<caret>foo.bar foo.bar bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>.bar foo.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo.bar foo<caret>.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo.bar foo.bar<caret> bar")
        assertEquals("foo.bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo.bar foo<caret>.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret>.bar foo.bar bar")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    fun `test Search current word shortcut with reverse search works`() {
        myFixture.configureByText(FILE, "foo bar <caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
    }

    fun `test Search current word with reverse search works several times`() {
        myFixture.configureByText(FILE, "foo bar <caret>foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo bar")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo bar")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current word with camel case works as expected`() {
        myFixture.configureByText(FILE, "<caret>fooBarFoo")
        myFixture.editor.settings.isCamelWords = true

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret>BarFoo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("fooBar<caret>Foo")
        assertEquals("fooBar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret>BarFoo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>fooBarFoo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Combination of typed letters and current word search works as expected`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("fo")
        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
    }

    fun `test Combination of typed letters and current word reverse search works as expected`() {
        myFixture.configureByText(FILE, "bar foo bar <caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("ba")
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("ba", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>bar foo bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("ba", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("bar foo <caret>bar foo")
        assertEquals("b", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("bar foo bar <caret>foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Search current line works`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo bar foo
               |baz bar foo
               |baz bar baz
               |foo bar foo
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_LINE)
        myFixture.checkResult(
            """foo bar foo<caret>
            |baz bar foo
            |baz bar baz
            |foo bar foo
            """.trimMargin()
        )
        assertEquals("foo bar foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """foo bar foo
            |baz bar foo
            |baz bar baz
            |foo bar foo<caret>
            """.trimMargin()
        )
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Backspace works as expected`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("oo")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("o", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Backspace works as expected after failed search`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("bar")
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)

        myFixture.type("rab")
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barrab", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barra", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("barr", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.FAILED, ISearchHandler.delegate?.state)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo ba<caret>r foo")
        assertEquals("ba", ISearchHandler.delegate?.text)
        assertEquals(ISearchState.SEARCH, ISearchHandler.delegate?.state)
    }

    fun `test Changing direction works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("oo")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar f<caret>oo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Changing direction works 2`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("oo")
        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("oo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("oo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Regexp search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Regexp backward search works`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.type("o{2}")
        myFixture.checkResult("foo bar f<caret>oo")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Changing direction in regexp search works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.checkResult("foo bar f<caret>oo")
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_BACKWARD)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Pasting from clipboard works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_EDITOR_PASTE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Pasting with plugin paste works`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Pasting from clipboard works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        CopyPasteManager.getInstance().setContents(StringSelection("ar"))

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("b")
        myFixture.checkResult("foo b<caret>ar foo")
        assertEquals("b", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_PASTE)
        myFixture.checkResult("foo bar<caret> foo")
        assertEquals("bar", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo b<caret>ar foo")
        assertEquals("b", ISearchHandler.delegate?.text)
    }

    fun `test Text iSearch key binding works during regexp search`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_REGEXP_FORWARD)
        myFixture.type("o{2}")
        myFixture.checkResult("foo<caret> bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("o{2}", ISearchHandler.delegate?.text)
    }

    fun `test Breadcrumb works as expected when changing direction`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo bar foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret> bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("fo<caret>o bar foo")
        assertEquals("fo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("f<caret>oo bar foo")
        assertEquals("f", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("", ISearchHandler.delegate?.text)
    }

    fun `test Breadcrumb works as expected when changing direction 2`() {
        myFixture.configureByText(FILE, "<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult("foo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    fun `test Simple reverse search works`() {
        myFixture.configureByText(FILE, "foo foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Simple reverse search works 2`() {
        myFixture.configureByText(FILE, "foo foo<caret> ")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo <caret>foo ")
        assertEquals("foo", ISearchHandler.delegate?.text)
    }

    fun `test Search starts at prompt`() {
        myFixture.configureByText(FILE, "foo<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("f")
        myFixture.checkResult("foof<caret>oo")
        myFixture.type("o")
        myFixture.checkResult("foofo<caret>o")
        myFixture.type("o")
        myFixture.checkResult("foofoo<caret>")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Reverse search starts at prompt`() {
        myFixture.configureByText(FILE, "foo<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("f")
        myFixture.checkResult("<caret>foofoo")
        myFixture.type("o")
        myFixture.checkResult("<caret>foofoo")
        myFixture.type("o")
        myFixture.checkResult("<caret>foofoo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Change direction happens at prompt`() {
        myFixture.configureByText(FILE, "<caret>foofoo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret>foo")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foofoo")

        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Simple reverse search works when not at end of document`() {
        myFixture.configureByText(FILE, "foo<caret> bar")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("oo")
        myFixture.checkResult("f<caret>oo bar")
        assertEquals("oo", ISearchHandler.delegate?.text)
    }

    fun `test Search with new line works`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo
                    |foo
                    |bar
                    |baz
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.performEditorAction(ACTION_ISEARCH_NEWLINE)
        myFixture.type("bar")

        myFixture.checkResult(
            """foo
              |foo
              |bar<caret>
              |baz
            """.trimMargin()
        )
    }

    fun `test Multiple reverse searches work`() {
        myFixture.configureByText(FILE, "foo bar foo<caret>")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("f")
        myFixture.checkResult("foo bar <caret>foo")
        myFixture.type("o")
        myFixture.checkResult("foo bar <caret>foo")
        myFixture.type("o")
        myFixture.checkResult("foo bar <caret>foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 2), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult("<caret>foo bar foo")
        assertEquals("foo", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Multiple caret search works over same texts`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo bar foo
            |<caret>foo bar baz
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        myFixture.checkResult(
            """foo<caret> bar foo
            |foo<caret> bar baz
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_WORD)
        myFixture.checkResult(
            """foo bar<caret> foo
            |foo bar<caret> baz
            """.trimMargin()
        )
        assertEquals("foo bar", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Multiple caret search works over different texts`() {
        myFixture.configureByText(
            FILE,
            """(<caret>foo bar) baz
            |(<caret>foo baz) bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type(")")

        myFixture.checkResult(
            """(foo bar)<caret> baz
            |(foo baz)<caret> bar
            """.trimMargin()
        )
        assertEquals(")", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 2), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Multiple caret search can be reversed after failed search`() {
        myFixture.configureByText(
            FILE,
            """fool (<caret>foo bar) baz
            |(<caret>foo baz) bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("fool")

        myFixture.checkResult(
            """fool (foo<caret> bar) baz
            |(foo<caret> baz) bar
            """.trimMargin()
        )
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(0, 1), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)

        myFixture.checkResult(
            """<caret>fool (foo bar) baz
            |(foo baz) bar
            """.trimMargin()
        )
        assertEquals("fool", ISearchHandler.delegate?.text)
        assertEquals(Pair(1, 1), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Multiple caret search works over overlapping texts`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo bar baz
            |<caret>foo bar bax
            |foo bar bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        myFixture.checkResult(
            """foo<caret> bar baz
            |foo<caret> bar bax
            |foo bar bay
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """foo bar baz
            |foo<caret> bar bax
            |foo<caret> bar bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """foo<caret> bar baz
            |foo<caret> bar bax
            |foo bar bay
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Reverse multiple caret search works over overlapping texts`() {
        myFixture.configureByText(
            FILE,
            """foo bar baz
            |foo bar bax<caret>
            |foo bar<caret> bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")

        myFixture.checkResult(
            """foo bar baz
            |<caret>foo bar bax
            |<caret>foo bar bay
            """.trimMargin()
        )
        assertEquals("foo", ISearchHandler.delegate?.text)

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.checkResult(
            """<caret>foo bar baz
            |<caret>foo bar bax
            |foo bar bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """foo bar baz
            |<caret>foo bar bax
            |<caret>foo bar bay
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Mark is set when search starts`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo bar baz
            |foo bar bax
            |foo bar bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult(
            """foo bar baz
            |foo bar bax
            |foo<caret> bar bay
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """<caret>foo bar baz
            |foo bar bax
            |foo bar bay
            """.trimMargin()
        )
    }

    fun `test Swap at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    fun `test Swap at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("<caret>foo foo bar baz")
    }

    fun `test Swap at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_SWAP)
        myFixture.checkResult("foo foo<caret> bar baz")
    }

    fun `test Mark at stop works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo</selection><caret> foo bar baz")
    }

    fun `test Mark at stop after failed search works`() {
        myFixture.configureByText(FILE, "<caret>foo foo bar baz")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foobar")
        myFixture.checkResult("foo<caret> foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("<selection>foo<caret></selection> foo bar baz")
    }

    fun `test Mark at stop after backward search works`() {
        myFixture.configureByText(FILE, "foo foo bar <caret>baz")

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo <caret>foo bar baz")
        myFixture.performEditorAction(ACTION_ISEARCH_MARK)
        myFixture.checkResult("foo <selection><caret>foo</selection> bar baz")
    }

    fun `test Isearch with lax search works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.lax = true

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("o e")
        myFixture.checkResult("foo bar ye<caret>s sir")
    }

    fun `test Isearch with lax search works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar yes sir")
        ISearchHandler.lax = true

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("o e i")
        myFixture.checkResult("foo bar yes si<caret>r")
    }

    fun `test Search can be toggled from lax to non-lax`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo bar")
        ISearchHandler.lax = true

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("f r")
        myFixture.checkResult("foo bar<caret> foo bar foo bar")
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")

        myFixture.performEditorAction(ACTION_TOGGLE_LAX_SEARCH)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")
    }

    fun `test Search can be toggled from non-lax to lax`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo bar foo bar")
        ISearchHandler.lax = false

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("f r")
        myFixture.checkResult("f<caret>oo bar foo bar foo bar")

        myFixture.performEditorAction(ACTION_TOGGLE_LAX_SEARCH)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo bar foo bar<caret> foo bar")
    }

    fun `test Search can be toggled from undefined case to case sensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> Foo foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> bar")
    }

    fun `test Search can be toggled from case sensitive to case insensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")
        myFixture.checkResult("foo<caret> Foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo Foo<caret> bar")
    }

    fun `test Search can be toggled from undefined case to case insensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("Foo")
        myFixture.checkResult("foo Foo<caret> foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> Foo bar")
    }

    fun `test Search can be toggled from case insensitive to case sensitive`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("Foo")
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo foo Foo<caret> bar")
    }

    fun `test Case sensitivity state is remembered in breadcrumbs`() {
        myFixture.configureByText(FILE, "<caret>foo Foo foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("Foo")
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_TOGGLE_CASE)
        assertEquals(CaseType.SENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo foo Foo<caret> bar")

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        assertEquals(CaseType.INSENSITIVE, ISearchHandler.delegate?.caseType)
        myFixture.checkResult("foo Foo<caret> foo foo Foo bar")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.checkResult("foo Foo foo<caret> foo Foo bar")
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import<caret>
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |import<caret>
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """
            |import
            |import<caret>
            |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import<caret>
            |import
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |import<caret>
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("purport")
        myFixture.checkResult(
            """
            |import
            |import
            |imp<caret>ort
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |import
            |import
            |imp<caret>ort
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |import<caret>
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |<caret>import
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """
            |import
            |<caret>import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("purport")
        myFixture.checkResult(
            """
            |im<caret>port
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |im<caret>port
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_FIRST)
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(1, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |<caret>import
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """
            |import
            |<caret>import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import
            |<caret>import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("purport")
        myFixture.checkResult(
            """
            |import
            |im<caret>port
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |im<caret>port
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_BACKWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |<caret>import
            |import
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |<caret>import
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import<caret>
            |import
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.checkResult(
            """
            |import
            |import<caret>
            |import
            """.trimMargin()
        )
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("purport")
        myFixture.checkResult(
            """
            |import
            |imp<caret>ort
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |imp<caret>ort
            |import
            """.trimMargin()
        )
        assertEquals(Pair(0, 0), ISearchHandler.delegate?.ui?.count)
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

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("import")
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(0, 3), ISearchHandler.delegate?.ui?.count)

        myFixture.performEditorAction(ACTION_ISEARCH_LAST)
        myFixture.checkResult(
            """
            |import
            |import
            |import<caret>
            """.trimMargin()
        )
        assertEquals(Pair(3, 3), ISearchHandler.delegate?.ui?.count)
    }

    private fun pressEnter() {
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
        ISearchHandler.delegate?.hide()
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
