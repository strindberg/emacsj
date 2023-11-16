package com.github.strindberg.emacsj.space

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_DELETE_LINES = "com.github.strindberg.emacsj.actions.space.deletelines"

class DeleteLinesTest : BasePlatformTestCase() {

    fun `testDo nothing on non-empty lines`() {
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

    fun `testDo nothing on non-empty lines 2`() {
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

    fun `testDelete all empty lines after non-empty line`() {
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

    fun `testOnly empty lines are deleted, not all whitespace`() {
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

    fun `testEmpty lines at document end are deleted`() {
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

    fun `testIsolated blank line is deleted`() {
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

    fun `testIsolated blank line at document end is not deleted (mimic Emacs)`() {
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

    fun `testIsolated blank line at document start is deleted`() {
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

    fun `testConsecutive blank lines are reduced 1`() {
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

    fun `testConsecutive blank lines are reduced 2`() {
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

    fun `testConsecutive blank lines are reduced 3`() {
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

    fun `testConsecutive blank lines at document end are reduced`() {
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

    fun `testConsecutive blank lines at document end are reduced 2`() {
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

    fun `testConsecutive blank lines at document start are reduced`() {
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
