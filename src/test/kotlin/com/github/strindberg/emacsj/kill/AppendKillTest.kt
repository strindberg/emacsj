package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
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
}
