package com.github.strindberg.emacsj.mark

import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_END
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_EXCHANGE_MARK = "com.github.strindberg.emacsj.actions.mark.exchangepointandmark"
private const val ACTION_PUSH_MARK = "com.github.strindberg.emacsj.actions.mark.pushmark"
private const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"

class MarkTest : BasePlatformTestCase() {

    fun `test Set mark and pop mark works`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("foo bar baz<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foo bar baz")
    }

    fun `test Pressing mark twice slowly starts selection`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        Thread.sleep(TIMEOUT + 100)
        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("<selection>foo bar baz</selection><caret>")
    }

    fun `test Exchange mark and point works`() {
        MarkHandler.editorTypeId = ""
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
        MarkHandler.editorTypeId = ""
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
