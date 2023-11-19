package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_PASTE_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.rectanglepaste"

class RectanglePasteTest : BasePlatformTestCase() {

    fun `test Paste works 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult("foobar<caret>")
    }

    fun `test Paste works 02`() {
        myFixture.configureByText(
            FILE,
            """<caret>foo
               |bar
               |taz
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """FOO
                  |BAR
                  |TAZ
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """FOOfoo
               |BARbar
               |TAZ<caret>taz
            """.trimMargin()
        )
    }

    fun `test Paste works 03`() {
        myFixture.configureByText(
            FILE,
            """aaa
               |a<caret>aa
               |aaa
               |aaa
               |aaa
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """bbb
                  |bbb
                  |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """aaa
              |abbbaa
              |abbbaa
              |abbb<caret>aa
              |aaa
            """.trimMargin()
        )
    }

    fun `test Paste works 04`() {
        myFixture.configureByText(
            FILE,
            """<caret>a
               |a
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """bbb
                  |bbb
                  |bbb
                  |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """bbba
              |bbba
              |bbb
              |bbb<caret>
            """.trimMargin()
        )
    }

    fun `test Paste works 05`() {
        myFixture.configureByText(
            FILE,
            """aa<caret>aaa
               |
               |aaa
               |
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """bbb
                  |bbb
                  |bbb
                  |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """aabbbaaa
              |  bbb
              |aabbba
              |  bbb<caret>
            """.trimMargin()
        )
    }
}
