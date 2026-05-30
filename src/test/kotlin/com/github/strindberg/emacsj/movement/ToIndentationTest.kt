package com.github.strindberg.emacsj.movement

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val FILE = "indentationfile.txt"

class ToIndentationTest : BasePlatformTestCase() {

    fun `test Caret is moved back to indentation`() {
        myFixture.configureByText(
            FILE,
            """
                |foobar
                |    baz<caret>buz
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_TO_INDENTATION)
        myFixture.checkResult(
            """
                |foobar
                |    <caret>bazbuz
            """.trimMargin()
        )
    }

    fun `test Caret is moved forward to indentation`() {
        myFixture.configureByText(
            FILE,
            """
                |foobar
                | <caret>   bazbuz
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_TO_INDENTATION)
        myFixture.checkResult(
            """
                |foobar
                |    <caret>bazbuz
            """.trimMargin()
        )
    }

    fun `test Caret is not moved if no non-whitespace on line`() {
        myFixture.configureByText(
            FILE,
            """
                |foobar
                | <caret> 
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_TO_INDENTATION)
        myFixture.checkResult(
            """
                |foobar
                | <caret> 
            """.trimMargin()
        )
    }

    fun `test Multiple carets are moved back to indentation`() {
        myFixture.configureByText(
            FILE,
            """
                |foobar
                |    bazbuz<caret>
                |    wiz<caret>woz
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_TO_INDENTATION)
        myFixture.checkResult(
            """
                |foobar
                |    <caret>bazbuz
                |    <caret>wizwoz
            """.trimMargin()
        )
    }
}
