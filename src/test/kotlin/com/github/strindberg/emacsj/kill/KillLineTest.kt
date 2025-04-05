package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

const val FILE = "file.txt"

private const val ACTION_KILL_LINE = "com.github.strindberg.emacsj.actions.kill.line"

class KillLineTest : BasePlatformTestCase() {

    fun `test Line is killed to line end`() {
        myFixture.configureByText(
            FILE,
            """baz<caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """baz<caret>
              |bar
            """.trimMargin()
        )
        TestCase.assertEquals("zoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Line is killed including new line when caret at line start`() {
        myFixture.configureByText(
            FILE,
            """<caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """<caret>bar
            """.trimMargin()
        )
        TestCase.assertEquals("zoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Line is killed including new line when only whitespace after caret`() {
        myFixture.configureByText(
            FILE,
            """zoo<caret>    
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """zoo<caret>bar
            """.trimMargin()
        )
        TestCase.assertEquals("    \n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test End of document is properly handled`() {
        myFixture.configureByText(
            FILE,
            """zoo    
               |bar
            <caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """zoo    
               |bar
            <caret>
            """.trimMargin()
        )
    }
}
