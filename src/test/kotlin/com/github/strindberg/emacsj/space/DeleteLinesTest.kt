package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val ACTION_DELETE_LINES = "com.github.strindberg.emacsj.actions.space.deletelines"

@RunWith(JUnit4::class)
class DeleteLinesTest : BasePlatformTestCase() {

    @Test
    fun `Do nothing on non-empty lines`() {
        myFixture.configureByText(
            FILE,
            """foo
            |bar<caret>
            |baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo
            |bar<caret>
            |baz
            """.trimMargin()
        )
    }

    @Test
    fun `Do nothing on non-empty lines 2`() {
        myFixture.configureByText(
            FILE,
            """foo
            |bar
            |baz<caret>
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo
            |bar
            |baz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Delete all empty lines after non-empty line`() {
        myFixture.configureByText(
            FILE,
            """foo
            |bar<caret>
            |
            |  
            |baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo
            |bar<caret>
            |baz
            """.trimMargin()
        )
    }

    @Test
    fun `Only empty lines are deleted, not all whitespace`() {
        myFixture.configureByText(
            FILE,
            """foo<caret> bar
            |  
            |
            |  baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo<caret> bar
            |  baz
            """.trimMargin()
        )
    }

    @Test
    fun `Empty lines at document end are deleted`() {
        myFixture.configureByText(
            FILE,
            """foo bar<caret>
            |
            |  
            |
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult("foo bar<caret>\n")
    }

    @Test
    fun `Isolated blank line is deleted`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |<caret>
            |  baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>  baz
            """.trimMargin()
        )
    }

    @Test
    fun `Isolated blank line at document end is not deleted (mimic Emacs)`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |  <caret>
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Isolated blank line at document start is deleted`() {
        myFixture.configureByText(
            FILE,
            """<caret>
            |foo bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """<caret>foo bar
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines are reduced 1`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |<caret>
            |
            |   
            |  baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            |  baz
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines are reduced 2`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |
            |<caret>
            |
            |  baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            |  baz
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines are reduced 3`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |
            |
            |<caret>
            |  baz
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            |  baz
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines at document end are reduced`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |<caret>
            |
            |
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines at document end are reduced 2`() {
        myFixture.configureByText(
            FILE,
            """foo bar
            |
            |
            |<caret>
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """foo bar
            |<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Consecutive blank lines at document start are reduced`() {
        myFixture.configureByText(
            FILE,
            """<caret>
            |
            |
            |foo bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_DELETE_LINES)
        myFixture.checkResult(
            """<caret>
               |foo bar
            """.trimMargin()
        )
    }
}
