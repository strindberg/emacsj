package com.github.strindberg.emacsj.paste

import com.github.strindberg.emacsj.actions.paste.ACTION_PASTE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jdesktop.swingx.plaf.basic.core.BasicTransferable

const val FILE = "file.txt"

private const val ACTION_PREFIX_PASTE = "com.github.strindberg.emacsj.actions.paste.pasteprefix"
private const val ACTION_HISTORY_PASTE = "com.github.strindberg.emacsj.actions.paste.pastehistory"

class PasteTest : BasePlatformTestCase() {

    fun `testPaste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("foobar<caret>")
    }

    fun `testPaste works with selection`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))

        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult("BARbar<caret>BAZ")
    }

    fun `testPrefix paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("foo<caret>bar")
    }

    fun `testPrefix paste works with selection`() {
        myFixture.configureByText(FILE, "BAR<selection>foo</selection>BAZ<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)

        myFixture.checkResult("BAR<caret>barBAZ")
    }

    fun `testPaste history after paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")
    }

    fun `testPaste history after prefix paste works`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))

        myFixture.performEditorAction(ACTION_PREFIX_PASTE)
        myFixture.checkResult("foo<caret>baz")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foo<caret>bar")
    }

    fun `testPaste history is cleared of duplicates`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")
    }

    fun `testPaste history is rotated`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(BasicTransferable("zed", null))
        Thread.sleep(100)
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null)) // discarded duplicate
        Thread.sleep(100)
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))
        Thread.sleep(100)
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))
        Thread.sleep(100)

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobar<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foozed<caret>")

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobaz<caret>")
    }

    fun `testPaste history is not invoked after movement`() {
        myFixture.configureByText(FILE, "foo<caret>BAZ")
        CopyPasteManager.getInstance().setContents(BasicTransferable("bar", null))
        CopyPasteManager.getInstance().setContents(BasicTransferable("baz", null))

        myFixture.performEditorAction(ACTION_PASTE)
        myFixture.checkResult("foobaz<caret>BAZ")

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)

        myFixture.performEditorAction(ACTION_HISTORY_PASTE)
        myFixture.checkResult("foobazB<caret>AZ")
    }
}
