package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_OPEN_LINE = "com.github.strindberg.emacsj.actions.space.openline"

class OpenLineTest : BasePlatformTestCase() {

    fun `testOpen in middle of line`() {
        myFixture.configureByText(FILE, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("foo<caret>\nbar")
    }

    fun `testOpen at beginning of line`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("<caret>\nfoobar")
    }

    fun `testOpen at end of line`() {
        myFixture.configureByText(FILE, "foobar<caret>")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("foobar<caret>\n")
    }
}
