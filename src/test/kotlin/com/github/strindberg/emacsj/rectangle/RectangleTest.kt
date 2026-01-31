package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

class RectangleTest : BasePlatformTestCase() {

    fun `test copy works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("foo<caret>")
    }

    fun `test Copy works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>foo")
    }

    fun `test Copy works 03`() {
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

    fun `test Copy works 04`() {
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

    fun `test Copy works 05`() {
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

    fun `test Copy works 06`() {
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

    fun `test Copy works 07`() {
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

    fun `test Copy works 08`() {
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

    fun `test Copy works 09`() {
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

    fun `test Cut works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    fun `test Cut works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    fun `test Cut works 03`() {
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

    fun `test Cut works 04`() {
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

    fun `test Cut works 05`() {
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

    fun `test Cut works 06`() {
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

    fun `test Cut works 07`() {
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

    fun `test Cut works 08`() {
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

    fun `test Cut works 09`() {
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

    fun `test Open works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    fun `test Open works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    fun `test Open works 03`() {
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

    fun `test Open works 04`() {
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

    fun `test Open works 05`() {
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

    fun `test Open works 06`() {
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

    fun `test Open works 07`() {
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

    fun `test Open works 08`() {
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

    fun `test Open works 09`() {
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

    fun `test Clear works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    fun `test Clear works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    fun `test Clear works 03`() {
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

    fun `test Clear works 04`() {
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

    fun `test Clear works 05`() {
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

    fun `test Clear works 06`() {
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

    fun `test Clear works 07`() {
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

    fun `test Clear works 08`() {
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

    fun `test Clear works 09`() {
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

    fun `test Keep works 01`() {
        myFixture.configureByText(FILE, "bar<selection>foo</selection><caret>baz")

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult("foo<caret>")
    }

    fun `test Keep works 02`() {
        myFixture.configureByText(FILE, "bar<caret><selection>foo</selection>baz")

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult("foo<caret>")
    }

    fun `test Keep works 03`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """foo
               |bar
               |baz<caret>
               |omf
            """.trimMargin()
        )
    }

    fun `test Keep works 04`() {
        myFixture.configureByText(
            FILE,
            """foo
               |FOO<selection>bar
               |BARba
               |BARbaz</selection><caret>BAZ
               |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """foo
               |bar
               |ba
               |baz<caret>
               |omf
            """.trimMargin()
        )
    }

    fun `test Keep works 05`() {
        myFixture.configureByText(
            FILE,
            """<selection>foo
               |bar
               |baz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """foo
               |bar
               |baz<caret>
            """.trimMargin()
        )
    }
}
