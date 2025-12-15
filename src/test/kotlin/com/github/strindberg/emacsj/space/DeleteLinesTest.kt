package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DeleteLinesTest : BasePlatformTestCase() {

    fun `test Do nothing on non-empty lines`() {
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

    fun `test Do nothing on non-empty lines 2`() {
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

    fun `test Delete all empty lines after non-empty line`() {
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

    fun `test Only empty lines are deleted, not all whitespace`() {
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

    fun `test Empty lines at document end are deleted`() {
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

    fun `test Isolated blank line is deleted`() {
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

    fun `test Isolated blank line at document end is not deleted (mimic Emacs)`() {
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

    fun `test Isolated blank line at document start is deleted`() {
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

    fun `test Consecutive blank lines are reduced 1`() {
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

    fun `test Consecutive blank lines are reduced 2`() {
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

    fun `test Consecutive blank lines are reduced 3`() {
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

    fun `test Consecutive blank lines at document end are reduced`() {
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

    fun `test Consecutive blank lines at document end are reduced 2`() {
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

    fun `test Consecutive blank lines at document start are reduced`() {
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
