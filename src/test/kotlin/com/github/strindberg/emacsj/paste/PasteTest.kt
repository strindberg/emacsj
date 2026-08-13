package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.kill.ACTION_CUT
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.intellij.ide.ClientCopyPasteManager
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_COPY
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.ide.CopyPasteManager
import org.junit.jupiter.api.Test

private const val FILE = "pastefile.txt"

class PasteTest : EmacsJTestCase() {

    @Test
    fun `Paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    @Test
    fun `Paste works with selection`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("BARbar<caret>BAZ")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("BAR<caret>barBAZ")
    }

    @Test
    fun `Prefix paste works 1`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("foo<caret>bar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobar<caret>")
    }

    @Test
    fun `Prefix paste works 2`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("foo<caret>bar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobar<caret>")
    }

    @Test
    fun `Prefix paste works with selection 1`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("BAR<caret>barBAZ")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("BARbar<caret>BAZ")
    }

    @Test
    fun `Prefix paste works with selection 2`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("BAR<caret>barBAZ")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("BARbar<caret>BAZ")
    }

    @Test
    fun `Paste history after paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobarbar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>barbar")
    }

    @Test
    fun `Paste history after prefix paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("barry"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foo<caret>barry")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobarry<caret>")
    }

    @Test
    fun `Paste history is cleared of duplicates`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")
    }

    @Test
    fun `Paste history is rotated`() {
        myFixture.configureByText(FILE, "foo<caret>")
        ClientCopyPasteManager.getCurrentInstance().removeIf { true }

        CopyPasteManager.getInstance().setContents(StringSelection("zed"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz")) // discarded duplicate
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foozed<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobaz<caret>")
    }

    @Test
    fun `Paste history is not invoked after movement`() {
        myFixture.configureByText(FILE, "foo<caret>BAZ")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>BAZ")

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobazB<caret>AZ")
    }

    @Test
    fun `Paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<caret>BAR
                |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("xxx"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult(
            """
                |fooxxx<caret>BAR
                |fooxxx<caret>BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                |foo<caret>xxxBAR
                |foo<caret>xxxBAZ
            """.trimMargin()
        )
    }

    @Test
    fun `Prefix paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<caret>BAR
                |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("xxx"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.checkResult(
            """
                |foo<caret>xxxBAR
                |foo<caret>xxxBAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                |foo<caret>xxxBAR
                |foo<caret>xxxBAZ
            """.trimMargin()
        )
    }

    @Test
    fun `Paste history with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<caret>BAR
                |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult(
            """
                |foobarbar<caret>BAR
                |foobarbar<caret>BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                |foo<caret>barbarBAR
                |foo<caret>barbarBAZ
            """.trimMargin()
        )
    }

    @Test
    fun `Paste history after prefix paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<caret>BAR
                |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult(
            """
                |foo<caret>barbarBAR
                |foo<caret>barbarBAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """
                |foo<caret>barbarBAR
                |foo<caret>barbarBAZ
            """.trimMargin()
        )
    }

    @Test
    fun `Paste after numeric universal argument works`() {
        myFixture.configureByText(FILE, "foo<selection>one</selection><caret>")
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.checkResult("foo<caret>")

        myFixture.configureByText(FILE, "foo<selection>two</selection><caret>")
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.checkResult("foo<caret>")

        myFixture.configureByText(FILE, "foo<selection>three</selection><caret>")
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.checkResult("foo<caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("footwo<caret>")
    }

    @Test
    fun `A multi-line clipboard goes in whole at every caret, not a line each`() {
        myFixture.configureByText(FILE, "a<caret>b\nc<caret>d")
        CopyPasteManager.getInstance().setContents(StringSelection("X\nY"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                aX
                Y<caret>b
                cX
                Y<caret>d
            """.trimIndent()
        )
    }

    @Test
    fun `More clipboard lines than carets loses nothing`() {
        myFixture.configureByText(FILE, "a<caret>b\nc<caret>d")
        CopyPasteManager.getInstance().setContents(StringSelection("X\nY\nZ"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                aX
                Y
                Z<caret>b
                cX
                Y
                Z<caret>d
            """.trimIndent()
        )
    }

    @Test
    fun `A line copied whole still lands on its own line at every caret`() {
        myFixture.configureByText(FILE, "alpha\nbe<caret>ta\ngamma")
        myFixture.performEditorAction(ACTION_EDITOR_COPY)

        myFixture.configureByText(FILE, "a<caret>b\nc<caret>d")
        myFixture.performEditorAction(ACTION_PASTE)

        // Left to the platform: a whole-line copy already reaches every caret intact, as a line of its own.
        myFixture.checkResult(
            """
                beta
                a<caret>b
                beta
                c<caret>d
            """.trimIndent()
        )
    }
}
