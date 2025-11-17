package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import com.github.strindberg.emacsj.EmacsJServiceImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

const val FILE = "file.txt"

class KillLineTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(EmacsJServiceImpl::class.java, EmacsJServiceImpl())
    }

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
        assertEquals("zoo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
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
        assertEquals("zoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
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
        assertEquals("    \n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
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

    fun `test Whole line is killed`() {
        myFixture.configureByText(
            FILE,
            """zed
               |baz<caret>zoo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)

        myFixture.checkResult(
            """zed
                |<caret>bar
            """.trimMargin()
        )
        assertEquals("bazzoo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test End of document is properly handled by kill whole line`() {
        myFixture.configureByText(
            FILE,
            """zoo    
               |bar
            <caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_WHOLE_LINE)

        myFixture.checkResult(
            """zoo    
               |bar
            |<caret>
            """.trimMargin()
        )
    }
}
