package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource
import kotlin.time.TimeSource
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.movement.ACTION_TEXT_END
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.github.strindberg.emacsj.word.ACTION_DELETE_NEXT_WORD
import com.github.strindberg.emacsj.word.ACTION_DELETE_PREVIOUS_WORD
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_TO
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.ide.CopyPasteManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FILE = "appendkillfile.txt"

private val THROTTLE_CLEARANCE_DURATION = 1000L.milliseconds

private val testTimeSource = TestTimeSource()

class AppendKillTest : EmacsJTestCase() {

    @BeforeEach
    fun installTestClock() {
        // CopyRegionHandler is shared between tests. Start every test well past the throttle window.
        testTimeSource += THROTTLE_CLEARANCE_DURATION
        CopyRegionHandler.timeSource = testTimeSource
    }

    @AfterEach
    fun restoreClock() {
        CopyRegionHandler.timeSource = TimeSource.Monotonic
    }

    @Test
    fun `Basic Copy works`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>baz</selection><caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY)

        myFixture.checkResult(
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )
        assertEquals("baz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Basic Copy with empty selection copies whole line`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY)

        myFixture.checkResult(
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `A whole-line copy repeated inside the throttle window is ignored`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)

        myFixture.performEditorAction(ACTION_COPY)

        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `A whole-line copy repeated after the throttle window copies the new line`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)

        testTimeSource += THROTTLE_DURATION + 1.milliseconds
        myFixture.performEditorAction(ACTION_COPY)

        assertEquals("bar", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `An explicit selection is copied even inside the throttle window`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )
        myFixture.performEditorAction(ACTION_COPY)

        myFixture.configureByText(FILE, "<selection>quux</selection><caret>")
        myFixture.performEditorAction(ACTION_COPY)

        assertEquals("quux", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Basic Cut works`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>baz</selection><caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT)

        myFixture.checkResult(
            """
                |<caret>zoo
                |bar
            """.trimMargin()
        )
        assertEquals("baz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Basic Cut with empty selection cuts whole line`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT)

        myFixture.checkResult(
            """
                |bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Copy works`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>baz</selection><caret>zoo
                |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                |bazzoo
                |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Cut works`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>baz</selection><caret>zoo
                |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                |zoo
                |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Copy where caret is before selection prepends new text`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret><selection>baz</selection>zoo
                |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_COPY)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                |bazzoo
                |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Cut where caret is before selection prepends new text`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret><selection>baz</selection>zoo
                |bar
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(StringSelection("zed"))

        myFixture.performEditorAction(ACTION_APPEND_NEXT_KILL)
        myFixture.performEditorAction(ACTION_CUT)
        myFixture.performEditorAction(ACTION_TEXT_END)
        myFixture.performEditorAction(ACTION_PASTE)

        myFixture.checkResult(
            """
                |zoo
                |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill with empty selection is ignored`() {
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

        assertEquals("zed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Kill line works`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                |baz
                |barzedzoo<caret>
            """.trimMargin()
        )
        assertEquals("zedzoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Kill whole line works`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                |barzedbazzoo
                |<caret>
            """.trimMargin()
        )
        assertEquals("zedbazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Delete next word works and word is appended`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                | zoo
                |barzedbaz<caret>
            """.trimMargin()
        )
        assertEquals("zedbaz", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Delete previous word works and word is prepended`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                | zoo
                |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Zap forward works and text is appended`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                |baz
                |barzed zoop<caret>
            """.trimMargin()
        )
        assertEquals("zed zoop", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    @Test
    fun `Append next kill before Zap backward works and text is prepended`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
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
            """
                |foo
                | zoop
                |barbazzed<caret>
            """.trimMargin()
        )
        assertEquals("bazzed", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }
}
