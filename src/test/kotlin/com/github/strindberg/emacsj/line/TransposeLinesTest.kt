package com.github.strindberg.emacsj.line

import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT0
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val FILE = "transposelinesfile.txt"

class TransposeLinesTest : EmacsJTestCase() {

    @Test
    fun `Transpose lines works as intended`() {
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

    @Test
    fun `Transpose lines on first line does nothing`() {
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

    @Test
    fun `Transpose lines on last line creates new line`() {
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

    @Test
    fun `Transpose lines with numeric prefix works as intended`() {
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

    @Test
    fun `Transpose lines with prefix 0 works as intended`() {
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

    @Test
    fun `Transpose lines reduces multiple carets to one and uses the primary caret`() {
        myFixture.configureByText(
            FILE,
            """
                foo
                bar<caret>
                baz<caret>
                baf
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_TRANSPOSE_LINES)

        assertEquals(1, myFixture.editor.caretModel.caretCount)
        myFixture.checkResult(
            """
                foo
                baz
                bar
                <caret>baf
            """.trimIndent()
        )
    }
}
