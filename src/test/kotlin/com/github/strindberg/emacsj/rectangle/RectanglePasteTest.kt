package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.ide.CopyPasteManager
import org.junit.jupiter.api.Test

private const val FILE = "rectanglepastefile.txt"

class RectanglePasteTest : EmacsJTestCase() {

    @Test
    fun `Paste works 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        CopyPasteManager.getInstance().setContents(StringSelection("bar"))

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult("foobar<caret>")
    }

    @Test
    fun `Paste works 02`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo
                |bar
                |taz
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """
                    |FOO
                    |BAR
                    |TAZ
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """
                |FOOfoo
                |BARbar
                |TAZ<caret>taz
            """.trimMargin()
        )
    }

    @Test
    fun `Paste works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |aaa
                |a<caret>aa
                |aaa
                |aaa
                |aaa
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """
                    |bbb
                    |bbb
                    |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """
                |aaa
                |abbbaa
                |abbbaa
                |abbb<caret>aa
                |aaa
            """.trimMargin()
        )
    }

    @Test
    fun `Paste works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>a
                |a
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """
                    |bbb
                    |bbb
                    |bbb
                    |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """
                |bbba
                |bbba
                |bbb
                |bbb<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Paste works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |aa<caret>aaa
                |
                |aaa
                |
            """.trimMargin()
        )
        CopyPasteManager.getInstance().setContents(
            StringSelection(
                """
                    |bbb
                    |bbb
                    |bbb
                    |bbb
                """.trimMargin()
            )
        )

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult(
            """
                |aabbbaaa
                |  bbb
                |aabbba
                |  bbb<caret>
            """.trimMargin()
        )
    }
}
