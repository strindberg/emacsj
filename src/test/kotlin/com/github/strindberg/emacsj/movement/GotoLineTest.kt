package com.github.strindberg.emacsj.movement

import java.awt.event.KeyEvent.VK_ENTER
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.intellij.openapi.editor.VisualPosition

private const val FILE = "gotofile.txt"

class GotoLineTest : EmacsJTestCase() {

    fun `test Goto Line works`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("2")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                <caret>bar
                baz
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line and column works`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("2:3")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                ba<caret>r
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line strips whitespace from arguments`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText(" 2 : 3 ")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                ba<caret>r
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line with argument 0 moves to first line`() {
        myFixture.configureByText(
            FILE,
            """
                foo
                bar
                <caret>baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("0")
        pressEnter()

        myFixture.checkResult(
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line with argument -10 moves to first line`() {
        myFixture.configureByText(
            FILE,
            """
                foo
                bar
                <caret>baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("-10")
        pressEnter()

        myFixture.checkResult(
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line with large argument moves to last line`() {
        myFixture.configureByText(
            FILE,
            """
                foo
                <caret>bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("100")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                bar
                <caret>baz
            """.trimIndent()
        )
    }

    fun `test Goto Line and column with negative column argument moves to first column`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("2:-1")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                <caret>bar
                baz
            """.trimIndent()
        )
    }

    fun `test Goto Line and column with large column argument moves to last column`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("2:100")
        pressEnter()

        myFixture.checkResult(
            """
                foo
                bar<caret>
                baz
            """.trimIndent()
        )
    }

    fun `test Non-numeric line argument is ignored`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("asdf")
        pressEnter()

        myFixture.checkResult(
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
    }

    fun `test Non-numeric column argument is ignored`() {
        myFixture.configureByText(
            FILE,
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
        myFixture.performEditorAction(ACTION_GOTO_LINE)

        setText("3:asdf")
        pressEnter()

        myFixture.checkResult(
            """
                <caret>foo
                bar
                baz
            """.trimIndent()
        )
    }

    fun `test Adding a caret cancels the goto line session`() {
        myFixture.configureByText(FILE, "<caret>aaa\nbbb")
        myFixture.performEditorAction(ACTION_GOTO_LINE)
        assertNotNull(GotoLineHandler.delegate)

        myFixture.editor.caretModel.addCaret(VisualPosition(1, 0))

        assertNull(GotoLineHandler.delegate)
    }

    fun `test Goto line reduces multiple carets to one`() {
        myFixture.configureByText(
            FILE,
            """
                |aa<caret>a
                |bb<caret>b
                |ccc
            """.trimMargin()
        )
        assertEquals(2, myFixture.editor.caretModel.caretCount)

        myFixture.performEditorAction(ACTION_GOTO_LINE)

        assertEquals(1, myFixture.editor.caretModel.caretCount)
    }

    private fun setText(text: String) {
        GotoLineHandler.delegate!!.ui.text = (text)
    }

    private fun pressEnter() {
        pressKey(GotoLineHandler.delegate?.ui, VK_ENTER)
    }
}
