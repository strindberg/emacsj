package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

const val FILE = "file.txt"

private const val ACTION_KILL_LINE = "com.github.strindberg.emacsj.actions.kill.kill"


// testa kill hamnar i clipboard
// kill tar resten av raden
// kill tar nästa rad om tomt på slutet
// kill tar nästa rad om på första kolumnen

class KillTest : BasePlatformTestCase() {

    fun `test Line is killed to line end`() {
        myFixture.configureByText(
            FILE,
            """baz<caret>foo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """baz<caret>
              |bar
            """.trimMargin()
        )
        TestCase.assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Line is killed including new line when caret at line start`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """<caret>bar
            """.trimMargin()
        )
        TestCase.assertEquals("foo\n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

    fun `test Line is killed including new line when only whitespace after caret`() {
        myFixture.configureByText(
            FILE,
            """foo<caret>    
               |bar
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KILL_LINE)

        myFixture.checkResult(
            """foo<caret>bar
            """.trimMargin()
        )
        TestCase.assertEquals("    \n", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor) as String)
    }

}
