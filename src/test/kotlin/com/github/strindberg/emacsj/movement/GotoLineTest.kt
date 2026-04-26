package com.github.strindberg.emacsj.movement

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import java.awt.event.KeyEvent.VK_ENTER
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class GotoLineTest : BasePlatformTestCase() {

    override fun tearDown() {
        GotoLineHandler.delegate?.hide()
        super.tearDown()
    }

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

    private fun setText(text: String) {
        GotoLineHandler.delegate!!.ui.text = (text)
    }

    private fun pressEnter() {
        val textField = GotoLineHandler.delegate!!.ui.textField
        GotoLineHandler.delegate!!.ui.popup.apply {
            dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_PRESSED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
            dispatchKeyEvent(KeyEvent(textField, KeyEvent.KEY_RELEASED, 1234L, 0, VK_ENTER, CHAR_UNDEFINED))
        }
    }
}
