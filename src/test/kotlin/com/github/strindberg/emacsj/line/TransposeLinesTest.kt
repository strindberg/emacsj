package com.github.strindberg.emacsj.line

import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class TransposeLinesTest : BasePlatformTestCase() {

    fun `test Transpose works`() {
        myFixture.configureByText(
            FILE,
            """
            foo
            bar<caret>
            baz
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        myFixture.checkResult(
            """
            bar
            foo
            <caret>baz
            """.trimIndent()
        )
    }
}
