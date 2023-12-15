package com.github.strindberg.emacsj.search

import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.actions.paste.ACTION_PASTE
import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ENTER
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_ISEARCH_BACKWARD = "com.github.strindberg.emacsj.actions.search.isearchtextbackward"
private const val ACTION_ISEARCH_FORWARD = "com.github.strindberg.emacsj.actions.search.isearchtextforward"
private const val ACTION_ISEARCH_PREVIOUS = "com.github.strindberg.emacsj.actions.search.isearchprevious"
private const val ACTION_ISEARCH_NEXT = "com.github.strindberg.emacsj.actions.search.isearchnext"
private const val ACTION_ISEARCH_REGEXP_FORWARD = "com.github.strindberg.emacsj.actions.search.isearchregexpforward"
private const val ACTION_ISEARCH_REGEXP_BACKWARD = "com.github.strindberg.emacsj.actions.search.isearchregexpbackward"
private const val ACTION_ISEARCH_WORD = "com.github.strindberg.emacsj.actions.search.isearchword"
private const val ACTION_ISEARCH_LINE = "com.github.strindberg.emacsj.actions.search.isearchline"
private const val ACTION_ISEARCH_CHAR = "com.github.strindberg.emacsj.actions.search.isearchchar"
private const val ACTION_ISEARCH_NEWLINE = "com.github.strindberg.emacsj.actions.search.isearchnewline"
private const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"

class ISearchTest : BasePlatformTestCase() {

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

    fun `test Reverse search - adding letters after finding matches works`() {
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

    fun `test Previous searches can be re-used`() {
        myFixture.configureByText(FILE, "<caret>foo foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.type("foo")

        pressEnter()

        myFixture.checkResult("foo<caret> foo")

        myFixture.performEditorAction(ACTION_ISEARCH_FORWARD)
        myFixture.performEditorAction(ACTION_ISEARCH_PREVIOUS)
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
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
        myFixture.type("z")
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
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
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
        myFixture.checkResult("foo fo<caret>oz foo")
        assertEquals("fo", ISearchHandler.delegate?.text)
        assertEquals(Pair(2, 3), ISearchHandler.delegate?.ui?.count)
    }

    fun `test Text can be pasted into previous search`() {
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
        myFixture.performEditorAction(ACTION_EDITOR_ENTER)
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
        MarkHandler.editorTypeId = ""
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
}
