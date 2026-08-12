package com.github.strindberg.emacsj.movement

import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import org.junit.jupiter.api.Test

private const val FILE = "movementfile.txt"

class TextMovementTest : EmacsJTestCase() {

    @Test
    fun `Text start sets mark`() {
        myFixture.configureByText(FILE, "foo<caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_START)
        myFixture.checkResult("<caret>foobar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Text end sets mark`() {
        myFixture.configureByText(FILE, "foo<caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Text start - no mark is pushed if selection is active`() {
        myFixture.configureByText(FILE, "foo<selection>baz</selection><caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_START)
        myFixture.checkResult("<caret>foobazbar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foobazbar")
    }

    @Test
    fun `Text end - no mark is pushed if selection is active`() {
        myFixture.configureByText(FILE, "foo<selection>baz</selection><caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.checkResult("foobazbar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobazbar<caret>")
    }
}
