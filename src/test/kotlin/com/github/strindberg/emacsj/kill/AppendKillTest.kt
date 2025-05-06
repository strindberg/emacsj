package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.movement.ACTION_TEXT_END
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.github.strindberg.emacsj.word.ACTION_DELETE_NEXT_WORD
import com.github.strindberg.emacsj.word.ACTION_DELETE_PREVIOUS_WORD
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_TO
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class AppendKillTest : BasePlatformTestCase() {

    fun `test Basic Copy works`() {
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

    fun `test Basic Copy with empty selection copies whole line`() {
        myFixture.configureByText(
            FILE,
            """baz<caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY)

        myFixture.checkResult(
            """baz<caret>zoo
              |bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Basic Cut works`() {
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

    fun `test Basic Cut with empty selection cuts whole line`() {
        myFixture.configureByText(
            FILE,
            """baz<caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT)

        myFixture.checkResult(
            """bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before Copy works`() {
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

    fun `test Append next kill before Cut works`() {
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

    fun `test Append next kill before Copy where caret is before selection prepends new text`() {
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

    fun `test Append next kill before Cut where caret is before selection prepends new text`() {
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
        myFixture.configureByText(
            FILE,
            """
            |baz
            |zoo
            |<caret>
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)

        assertEquals("zed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before Kill line works`() {
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

    fun `test Append next kill before Kill whole line works`() {
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

    fun `test Append next kill before Delete next word works and word is appended`() {
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

    fun `test Append next kill before Delete previous word works and word is prepended`() {
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

    fun `test Append next kill before Zap forward works and text is appended`() {
        myFixture.configureByText(
            FILE,
            """foo
               |baz<caret> zoop
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("p")
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              |baz
              |barzed zoop<caret>
            """.trimMargin()
        )
        assertEquals("zed zoop", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Append next kill before Zap backward works and text is prepended`() {
        myFixture.configureByText(
            FILE,
            """foo
               |baz<caret> zoop
               |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("b")
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """foo
              | zoop
              |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }
}
