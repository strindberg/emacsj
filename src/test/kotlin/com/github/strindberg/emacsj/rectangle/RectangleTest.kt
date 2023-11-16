package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_COPY_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.copyrectangle"
private const val ACTION_CUT_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.cutrectangle"
private const val ACTION_OPEN_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.openrectangle"
private const val ACTION_CLEAR_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.clearrectangle"

class RectangleTest : BasePlatformTestCase() {

    fun `testCopy works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("foo<caret>")
    }

    fun `testCopy works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>foo")
    }

    fun `testCopy works 03`() {
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

    fun `testCopy works 04`() {
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

    fun `testCopy works 05`() {
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

    fun `testCopy works 06`() {
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

    fun `testCopy works 07`() {
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

    fun `testCopy works 08`() {
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

    fun `testCopy works 09`() {
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

    fun `testCut works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    fun `testCut works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    fun `testCut works 03`() {
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

    fun `testCut works 04`() {
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

    fun `testCut works 05`() {
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

    fun `testCut works 06`() {
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

    fun `testCut works 07`() {
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

    fun `testCut works 08`() {
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

    fun `testCut works 09`() {
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

    fun `testOpen works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    fun `testOpen works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    fun `testOpen works 03`() {
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

    fun `testOpen works 04`() {
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

    fun `testOpen works 05`() {
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

    fun `testOpen works 06`() {
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

    fun `testOpen works 07`() {
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

    fun `testOpen works 08`() {
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

    fun `testOpen works 09`() {
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

    fun `testClear works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    fun `testClear works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    fun `testClear works 03`() {
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

    fun `testClear works 04`() {
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

    fun `testClear works 05`() {
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

    fun `testClear works 06`() {
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

    fun `testClear works 07`() {
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

    fun `testClear works 08`() {
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

    fun `testClear works 09`() {
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
