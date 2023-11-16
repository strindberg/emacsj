package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val ACTION_OPEN_LINE = "com.github.strindberg.emacsj.actions.space.openline"

@RunWith(JUnit4::class)
class OpenLineTest : BasePlatformTestCase() {

    @Test
    fun `Open in middle of line`() {
        myFixture.configureByText(FILE, "foo<caret>bar")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("foo<caret>\nbar")
    }

    @Test
    fun `Open at beginning of line`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("<caret>\nfoobar")
    }

    @Test
    fun `Open at end of line`() {
        myFixture.configureByText(FILE, "foobar<caret>")
        myFixture.performEditorAction(ACTION_OPEN_LINE)
        myFixture.checkResult("foobar<caret>\n")
    }
}
