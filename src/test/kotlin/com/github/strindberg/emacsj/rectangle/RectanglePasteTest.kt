package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.EditorTestUtil
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

    @Test
    fun `Paste counts a tab as the columns it is displayed with`() {
        myFixture.configureByText(FILE, "\t<caret>foo\nabcdefg")
        CopyPasteManager.getInstance().setContents(StringSelection("X\nY"))

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult("\tXfoo\nabcdY<caret>efg")
    }

    @Test
    fun `Paste goes into document lines when a line above is soft wrapped`() {
        myFixture.configureByText(FILE, "aaaa bbbb cccc dddd eeee\n<caret>x\ny")
        EditorTestUtil.configureSoftWraps(myFixture.editor, 10)
        CopyPasteManager.getInstance().setContents(StringSelection("1\n2"))

        myFixture.performEditorAction(ACTION_PASTE_RECTANGLE)

        myFixture.checkResult("aaaa bbbb cccc dddd eeee\n1x\n2<caret>y")
    }
}
