package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

const val FILE = "file.txt"

private const val ACTION_COPY_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.copyrectangle"
private const val ACTION_CUT_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.cutrectangle"
private const val ACTION_OPEN_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.openrectangle"
private const val ACTION_CLEAR_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.clearrectangle"

@RunWith(JUnit4::class)
class RectangleTest : BasePlatformTestCase() {

    @Test
    fun `copy works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `copy works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `copy works 03`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """bar
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
               |FOObar
               |BARbaz<caret>BAZ
               |omf
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 04`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>bar
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """bar
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foobar
            |barbaz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 05`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
            |
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
            |barbaz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 06`() {
        myFixture.configureByText(
            FILE,
            """<selection>foo
            |
            |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
            |
            |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 07`() {
        myFixture.configureByText(
            FILE,
            """a<selection>foo
            |b
            |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """afoo
            |b
            |cbar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 08`() {
        myFixture.configureByText(
            FILE,
            """<selection>fooa
            |
            |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """fooa
            |
            |bar<caret>c
            """.trimMargin()
        )
    }

    @Test
    fun `copy works 09`() {
        myFixture.configureByText(
            FILE,
            """   <selection>foo
            |      
            |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """foo
            |   
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """   foo
            |      
            |   bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `cut works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `cut works 03`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """bar
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
               |FOO
               |BAR<caret>BAZ
               |omf
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 04`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>bar
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """bar
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
            |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 05`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
            |
            |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """foo
            |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 06`() {
        myFixture.configureByText(
            FILE,
            """<selection>foo
            |
            |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
            |
            |
            |<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 07`() {
        myFixture.configureByText(
            FILE,
            """a<selection>foo
            |b
            |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """a
            |b
            |c<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 08`() {
        myFixture.configureByText(
            FILE,
            """<selection>fooa
            |
            |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """foo
            |
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """a
            |
            |<caret>c
            """.trimMargin()
        )
    }

    @Test
    fun `cut works 09`() {
        myFixture.configureByText(
            FILE,
            """   <selection>foo
            |      
            |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """foo
            |   
            |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
            |   
            |   
            |   <caret>
            """.trimMargin()
        )
    }

    @Test
    fun `open works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    @Test
    fun `open works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    @Test
    fun `open works 03`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """foo
               |FOO<caret>   bar
               |BAR   bazBAZ
               |omf
            """.trimMargin()
        )
    }

    @Test
    fun `open works 04`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>bar
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """foo<caret>   bar
            |bar   baz
            """.trimMargin()
        )
    }

    @Test
    fun `open works 05`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """foo<caret>
            |bar   baz
            """.trimMargin()
        )
    }

    @Test
    fun `open works 06`() {
        myFixture.configureByText(
            FILE,
            """<selection>foo
            |
            |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """<caret>   foo
            |
            |   bar
            """.trimMargin()
        )
    }

    @Test
    fun `open works 07`() {
        myFixture.configureByText(
            FILE,
            """a<selection>foo
            |b
            |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """a<caret>   foo
            |b
            |c   bar
            """.trimMargin()
        )
    }

    @Test
    fun `open works 08`() {
        myFixture.configureByText(
            FILE,
            """<selection>fooa
            |
            |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """<caret>   fooa
            |
            |   barc
            """.trimMargin()
        )
    }

    @Test
    fun `open works 09`() {
        myFixture.configureByText(
            FILE,
            """   <selection>foo
            |      
            |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """   <caret>   foo
            |         
            |      bar
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    @Test
    fun `clear works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    @Test
    fun `clear works 03`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """foo
               |FOO<caret>   
               |BAR   BAZ
               |omf
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 04`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>bar
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """foo<caret>   
            |bar   
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 05`() {
        myFixture.configureByText(
            FILE,
            """foo<selection>
            |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """foo<caret>
            |bar   
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 06`() {
        myFixture.configureByText(
            FILE,
            """<selection>foo
            |
            |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """<caret>   
            |
            |   
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 07`() {
        myFixture.configureByText(
            FILE,
            """a<selection>foo
            |b
            |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """a<caret>   
            |b
            |c   
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 08`() {
        myFixture.configureByText(
            FILE,
            """<selection>fooa
            |
            |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """<caret>   a
            |
            |   c
            """.trimMargin()
        )
    }

    @Test
    fun `clear works 09`() {
        myFixture.configureByText(
            FILE,
            """   <selection>foo
            |      
            |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """   <caret>   
            |      
            |      
            """.trimMargin()
        )
    }
}
