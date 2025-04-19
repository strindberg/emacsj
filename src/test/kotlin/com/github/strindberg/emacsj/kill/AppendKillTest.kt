package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.movement.ACTION_TEXT_END
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.github.strindberg.emacsj.word.ACTION_DELETE_NEXT_WORD
import com.github.strindberg.emacsj.word.ACTION_DELETE_PREVIOUS_WORD
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AppendKillTest : BasePlatformTestCase() {

    fun `test Basic copy works`() {
        myFixture.configureByText(
            FILE,
            """<selection>baz</selection><caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY)

        myFixture.checkResult(
            """baz<caret>zoo
              |bar
            """.trimMargin()
        )
        assertEquals("baz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Basic cut works`() {
        myFixture.configureByText(
            FILE,
            """<selection>baz</selection><caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT)

        myFixture.checkResult(
            """<caret>zoo
              |bar
            """.trimMargin()
        )
        assertEquals("baz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before copy works`() {
        myFixture.configureByText(
            FILE,
            """<selection>baz</selection><caret>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """bazzoo
              |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before cut works`() {
        myFixture.configureByText(
            FILE,
            """<selection>baz</selection><caret>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """zoo
              |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before copy where caret is before selection prepends new text`() {
        myFixture.configureByText(
            FILE,
            """<caret><selection>baz</selection>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """bazzoo
              |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before cut where caret is before selection prepends new text`() {
        myFixture.configureByText(
            FILE,
            """<caret><selection>baz</selection>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """zoo
              |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill with empty selection is ignored`() {
        myFixture.configureByText(FILE, "baz<selection></selection><caret>zoo")
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)

        assertEquals("zed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before kill line works`() {
        myFixture.configureByText(
            FILE,
            """foo
               |baz<caret>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_KILL_LINE)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              |baz
              |barzedzoo<caret>
            """.trimMargin()
        )
        assertEquals("zedzoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before kill whole line works`() {
        myFixture.configureByText(
            FILE,
            """foo
               |baz<caret>zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              |barzedbazzoo
              |<caret>
            """.trimMargin()
        )
        assertEquals("zedbazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before delete next word works`() {
        myFixture.configureByText(
            FILE,
            """foo
               |<caret>baz zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              | zoo
              |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before delete previous word works and word is appended`() {
        myFixture.configureByText(
            FILE,
            """foo
               |baz<caret> zoo
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              | zoo
              |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }
}
