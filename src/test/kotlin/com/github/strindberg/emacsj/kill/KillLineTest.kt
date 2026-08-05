package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.ide.CopyPasteManager

private const val FILE = "killlinefile.txt"

class KillLineTest : EmacsJTestCase() {

    fun `test Line is killed to line end`() {
        myFixture.configureByText(
            FILE,
            """
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """
                |baz<caret>
                |bar
            """.trimMargin()
        )
        assertEquals("zoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test Line is killed including new line when caret at line start`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """
                |<caret>bar
            """.trimMargin()
        )
        assertEquals("zoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test Line is killed including new line when only whitespace after caret`() {
        myFixture.configureByText(
            FILE,
            """
                |zoo<caret>    
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """
                |zoo<caret>bar
            """.trimMargin()
        )
        assertEquals("    \n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test End of document is properly handled`() {
        myFixture.configureByText(
            FILE,
            """
                |zoo    
                |bar
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """
                |zoo    
                |bar
                |<caret>
            """.trimMargin()
        )
    }

    fun `test Whole line is killed`() {
        myFixture.configureByText(
            FILE,
            """
                |zed
                |baz<caret>zoo
                |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)

        myFixture.checkResult(
            """
                |zed
                |<caret>bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test End of document is properly handled by kill whole line`() {
        myFixture.configureByText(
            FILE,
            """
                |zoo    
                |bar
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)

        myFixture.checkResult(
            """
                |zoo    
                |bar
                |<caret>
            """.trimMargin()
        )
    }

    fun `test Rest of last line is killed when the document has no trailing new line`() {
        myFixture.configureByText(FILE, "zed\nbaz<caret>zoo")

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult("zed\nbaz<caret>")
        assertEquals("zoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test Whole last line is killed when the document has no trailing new line`() {
        myFixture.configureByText(FILE, "zed\n<caret>bazzoo")

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult("zed\n<caret>")
        assertEquals("bazzoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }

    fun `test Kill rest of line works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |ba<caret>zzoo
                |fo<caret>obar
                |
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """
                |ba<caret>
                |fo<caret>
                |
            """.trimMargin()
        )
    }

    fun `test Kill whole line with multiple carets removes every line and merges the carets`() {
        myFixture.configureByText(
            FILE,
            """
                |ba<caret>zzoo
                |fo<caret>obar
                |keep
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)

        myFixture.checkResult("<caret>keep")
        assertEquals(1, myFixture.editor.caretModel.caretCount)
    }
}
