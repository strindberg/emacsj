package com.github.strindberg.emacsj.line

import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT0
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class TransposeLinesTest : BasePlatformTestCase() {

    override fun tearDown() {
        UniversalArgumentHandler.delegate?.hide()
        super.tearDown()
    }

    fun `test Transpose lines works as intended`() {
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

    fun `test Transpose lines on first line does nothing`() {
        myFixture.configureByText(
            FILE,
            """
            foo<caret>
            bar
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        myFixture.checkResult(
            """
            foo<caret>
            bar
            """.trimIndent()
        )
    }

    fun `test Transpose lines on last line creates new line`() {
        myFixture.configureByText(
            FILE,
            """
            foo
            bar
            baz<caret>
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        myFixture.checkResult(
            """
            foo
            baz
            bar
            <caret>
            """.trimIndent()
        )
    }

    fun `test Transpose lines with numeric prefix works as intended`() {
        myFixture.configureByText(
            FILE,
            """
            foo
            bar
            baz<caret>
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        myFixture.checkResult(
            """
            baz
            bar
            foo
            <caret>
            """.trimIndent()
        )
    }

    fun `test Transpose lines with prefix 0 works as intended`() {
        myFixture.configureByText(
            FILE,
            """
            foo<caret>
            bar
            baz
            baf
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        repeat(3) {
            myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)
        }
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        myFixture.checkResult(
            """
            baf
            bar
            baz
            foo
            <caret>
            """.trimIndent()
        )
    }
}
