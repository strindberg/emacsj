package com.github.strindberg.emacsj.space

import com.github.strindberg.emacsj.EmacsJServiceImpl
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

private const val FILE2 = "file2.txt"

class PrefixDeleteSpaceTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(EmacsJServiceImpl::class.java, EmacsJServiceImpl())
    }

    fun `test Nothing to delete`() {
        myFixture.configureByText(FILE2, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete one space forward`() {
        myFixture.configureByText(FILE2, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Delete several spaces forward`() {
        myFixture.configureByText(FILE2, "foo<caret>  bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>  bar")
    }

    fun `test Delete one space backward`() {
        myFixture.configureByText(FILE2, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete several spaces backward`() {
        myFixture.configureByText(FILE2, "foo  <caret>bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Delete spaces in both directions`() {
        myFixture.configureByText(FILE2, "foo  <caret>  bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>  bar")
    }

    fun `test Delete spaces at end of file`() {
        myFixture.configureByText(FILE2, "foo<caret>  ")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>  ")
    }

    fun `test Delete no space at end of file`() {
        myFixture.configureByText(FILE2, "foo<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Delete in both directions at end of file`() {
        myFixture.configureByText(FILE2, "foo  <caret>  ")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>  ")
    }

    fun `test Delete spaces at beginning of file`() {
        myFixture.configureByText(FILE2, "  <caret>foo")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Delete no space at beginning of file`() {
        myFixture.configureByText(FILE2, "<caret>foo")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Delete in both directions at beginning of file`() {
        myFixture.configureByText(FILE2, "  <caret>  foo")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("<caret>  foo")
    }

    fun `test Stop at end of line`() {
        myFixture.configureByText(FILE2, "foo<caret>  \n  bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo<caret>  \n  bar")
    }

    fun `test Stop at beginning of line`() {
        myFixture.configureByText(FILE2, "foo  \n  <caret>bar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_DELETE_SPACE)
        myFixture.checkResult("foo  \n<caret>bar")
    }
}
