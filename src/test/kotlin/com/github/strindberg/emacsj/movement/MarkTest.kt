package com.github.strindberg.emacsj.movement

import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_LINE_END
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

const val FILE = "file.txt"

private const val ACTION_EXCHANGE_MARK = "com.github.strindberg.emacsj.actions.movement.exchangepointandmark"
private const val ACTION_PUSH_MARK = "com.github.strindberg.emacsj.actions.movement.pushmark"
private const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.movement.popmark"

@RunWith(JUnit4::class)
class MarkTest : BasePlatformTestCase() {

    @Test
    fun `set mark and pop mark works`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.checkResult("foo bar baz<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("<caret>foo bar baz")
    }

    @Test
    fun `exchange mark and point works`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText(FILE, "A<caret>foo bar bazB")

        myFixture.performEditorAction(ACTION_PUSH_MARK)

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_LINE_END)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        myFixture.checkResult("A<selection>foo bar baz<caret></selection>B")

        myFixture.performEditorAction(ACTION_EXCHANGE_MARK)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        myFixture.checkResult("<caret><selection>Afoo bar baz</selection>B")
    }

    @Test
    fun `exchange mark and point reactivates selection`() {
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
