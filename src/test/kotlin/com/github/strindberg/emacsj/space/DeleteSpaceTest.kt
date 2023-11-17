package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_DELETE_SPACE = "com.github.strindberg.emacsj.actions.space.deletespace"

const val FILE = "file.txt"

class DeleteSpaceTest : BasePlatformTestCase() {

    fun `test Nothing to delete`() {
        myFixture.configureByText(FILE, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete one space forward`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete several spaces forward`() {
        myFixture.configureByText(FILE, "foo<caret>  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete one space backward`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete several spaces backward`() {
        myFixture.configureByText(FILE, "foo  <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete spaces in both directions`() {
        myFixture.configureByText(FILE, "foo  <caret>  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete spaces at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>  ")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Delete no space at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Delete in both directions at end of file`() {
        myFixture.configureByText(FILE, "foo  <caret>  ")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Delete spaces at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Delete no space at beginning of file`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Delete in both directions at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>  foo")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Stop at end of line`() {
        myFixture.configureByText(FILE, "foo<caret>  \n  bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>\n  bar")
    }

    fun `test Stop at beginning of line`() {
        myFixture.configureByText(FILE, "foo  \n  <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo  \n<caret>bar")
    }
}
