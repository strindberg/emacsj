package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_ONE_SPACE = "com.github.strindberg.emacsj.actions.space.onespace"

class OneSpaceTest : BasePlatformTestCase() {

    fun `testNothing to delete`() {
        myFixture.configureByText(FILE, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete one space forward`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete several spaces forward`() {
        myFixture.configureByText(FILE, "foo<caret>  bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete one space backward`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete several spaces backward`() {
        myFixture.configureByText(FILE, "foo  <caret>bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete spaces in both directions`() {
        myFixture.configureByText(FILE, "foo  <caret>  bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `testDelete spaces at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>  ")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>")
    }

    fun `testDelete no space at end of file`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>")
    }

    fun `testDelete in both directions at end of file`() {
        myFixture.configureByText(FILE, "foo  <caret>  ")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>")
    }

    fun `testDelete spaces at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>foo")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult(" <caret>foo")
    }

    fun `testDelete no space at beginning of file`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult(" <caret>foo")
    }

    fun `testDelete in both directions at beginning of file`() {
        myFixture.configureByText(FILE, "  <caret>  foo")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult(" <caret>foo")
    }

    fun `testStop at end of line`() {
        myFixture.configureByText(FILE, "foo<caret>  \n  bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo <caret>\n  bar")
    }

    fun `testStop at beginning of line`() {
        myFixture.configureByText(FILE, "foo  \n  <caret>bar")
        myFixture.performEditorAction(ACTION_ONE_SPACE)
        myFixture.checkResult("foo  \n <caret>bar")
    }
}
