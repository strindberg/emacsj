package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_END
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class MarkTest : BasePlatformTestCase() {

    override fun setUp() {
        MarkHandler.editorTypeId = ""
        super.setUp()
    }

    fun `test Set mark and pop mark works 1`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("<selection>foo bar baz</selection><caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foo bar baz")
    }

    fun `test Set mark and pop mark works 2`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("<selection>foo bar baz</selection><caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.checkResult("<caret>foo bar baz")
        UniversalArgumentHandler.delegate?.hide()
    }

    fun `test Pressing mark twice pushes mark without starting selection`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("foo bar baz<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foo bar baz")
    }

    fun `test Exchange mark and point works`() {
        myFixture.configureByText(FILE, "A<caret>foo bar bazB")

        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        myFixture.checkResult("A<selection>foo bar baz<caret></selection>B")

        myFixture.performEditorAction(ACTION_EXCHANGE_MARK)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        myFixture.checkResult("<caret><selection>Afoo bar baz</selection>B")

        myFixture.performEditorAction(ACTION_EXCHANGE_MARK)
        myFixture.checkResult("<selection>Afoo bar baz</selection><caret>B")
    }

    fun `test Exchange mark and point reactivates selection`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo bar baz
              |FOO BAR BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult(
            """foo bar baz<caret>
              |FOO BAR BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)
        myFixture.checkResult(
            """foo bar baz
              |FOO BAR BAZ<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_EXCHANGE_MARK)
        myFixture.checkResult(
            """<caret><selection>foo bar baz
              |FOO BAR BAZ</selection>
            """.trimMargin()
        )
    }
}
