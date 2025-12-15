package com.github.strindberg.emacsj.movement

import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.word.FILE
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TextMovementTest : BasePlatformTestCase() {

    fun `test Text start sets mark`() {
        myFixture.configureByText(FILE, "foo<caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_START)
        myFixture.checkResult("<caret>foobar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Text end sets mark`() {
        myFixture.configureByText(FILE, "foo<caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Text start - no mark is pushed if selection is active`() {
        myFixture.configureByText(FILE, "foo<selection>baz</selection><caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_START)
        myFixture.checkResult("<caret>foobazbar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foobazbar")
    }

    fun `test Text end - no mark is pushed if selection is active`() {
        myFixture.configureByText(FILE, "foo<selection>baz</selection><caret>bar")

        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.checkResult("foobazbar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobazbar<caret>")
    }
}
