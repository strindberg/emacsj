package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val ACTION_DELETE_SPACE = "com.github.strindberg.emacsj.actions.space.deletespace"

const val FILE = "file.txt"

@RunWith(JUnit4::class)
class DeleteSpaceTest : BasePlatformTestCase() {

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
}
