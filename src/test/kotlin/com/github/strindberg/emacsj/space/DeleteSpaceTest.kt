package com.github.strindberg.emacsj.space

import com.github.strindberg.emacsj.EmacsJTestCase
import org.junit.jupiter.api.Test

private const val FILE = "deletespacefile.txt"

class DeleteSpaceTest : EmacsJTestCase() {

    @Test
    fun `Nothing to delete`() {
        myFixture.configureByText(FILE, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete one space forward`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete several spaces forward`() {
        myFixture.configureByText(FILE, "foo<caret>  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete one space backward`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete several spaces backward`() {
        myFixture.configureByText(FILE, "foo  <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete spaces in both directions`() {
        myFixture.configureByText(FILE, "foo  <caret>  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Delete spaces at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>  ")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Delete no space at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Delete in both directions at end of file`() {
        myFixture.configureByText(FILE, "foo  <caret>  ")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Delete spaces at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Delete no space at beginning of file`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Delete in both directions at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>  foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Stop at end of line`() {
        myFixture.configureByText(FILE, "foo<caret>  \n  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>\n  bar")
    }

    @Test
    fun `Stop at beginning of line`() {
        myFixture.configureByText(FILE, "foo  \n  <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo  \n<caret>bar")
    }

    @Test
    fun `Delete space works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<caret>   bar
                |baz<caret>   qux
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DELETE_SPACE)

        myFixture.checkResult(
            """
                |foo<caret>bar
                |baz<caret>qux
            """.trimMargin()
        )
    }

    @Test
    fun `First character of the file is kept when there is no space to delete`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("f<caret>oo")
    }

    @Test
    fun `First character of the file is kept when deleting space after it`() {
        myFixture.configureByText(FILE, "f  <caret>oo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("f<caret>oo")
    }

    @Test
    fun `Space at the start of the file is deleted`() {
        myFixture.configureByText(FILE, "  <caret>foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }
}
