package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.actions.paste.ACTION_PASTE
import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_PREFIX_PASTE = "com.github.strindberg.emacsj.actions.paste.pasteprefix"
private const val ACTION_HISTORY_PASTE = "com.github.strindberg.emacsj.actions.paste.pastehistory"
private const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"
private const val ACTION_PUSH_MARK = "com.github.strindberg.emacsj.actions.mark.pushmark"

class PasteTest : BasePlatformTestCase() {

    override fun setUp() {
        MarkHandler.editorTypeId = ""
        super.setUp()
    }

    fun `test Paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `test Paste works with selection`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("BARbar<caret>BAZ")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("BAR<caret>barBAZ")
    }

    fun `test Prefix paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("foo<caret>bar")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobar<caret>")
    }

    fun `test Prefix paste works with selection`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("BAR<caret>barBAZ")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("BARbar<caret>BAZ")
    }

    fun `test Paste history after paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobarbar<caret>")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foo<caret>barbar")
    }

    fun `test Paste history after prefix paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("barry"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foo<caret>barry")

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult("foobarry<caret>")
    }

    fun `test Paste history is cleared of duplicates`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")
    }

    /* Clipboard access is unreliable when tests are run from the command line. This test is thus disabled for now. */
    fun `Paste history is rotated`() {
        myFixture.configureByText(FILE, "foo<caret>")
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

    fun `test Paste history is not invoked after movement`() {
        myFixture.configureByText(FILE, "foo<caret>BAZ")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>BAZ")

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobazB<caret>AZ")
    }

    fun `test Paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """foo<caret>BAR
            |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("xxx"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult(
            """fooxxx<caret>BAR
            |fooxxx<caret>BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """foo<caret>xxxBAR
            |foo<caret>xxxBAZ
            """.trimMargin()
        )
    }

    fun `test Prefix paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """foo<caret>BAR
            |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("xxx"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.checkResult(
            """foo<caret>xxxBAR
            |foo<caret>xxxBAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """foo<caret>xxxBAR
            |foo<caret>xxxBAZ
            """.trimMargin()
        )
    }

    fun `test Paste history with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """foo<caret>BAR
            |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult(
            """foobarbar<caret>BAR
            |foobarbar<caret>BAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """foo<caret>barbarBAR
            |foo<caret>barbarBAZ
            """.trimMargin()
        )
    }

    fun `test Paste history after prefix paste with multiple carets works as expected`() {
        myFixture.configureByText(
            FILE,
            """foo<caret>BAR
            |foo<caret>BAZ
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        CopyPasteManager.getInstance().setContents(StringSelection("barbar"))
        CopyPasteManager.getInstance().setContents(StringSelection("baz"))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult(
            """foo<caret>barbarBAR
            |foo<caret>barbarBAZ
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_POP_MARK)
        myFixture.checkResult(
            """foo<caret>barbarBAR
            |foo<caret>barbarBAZ
            """.trimMargin()
        )
    }
}
