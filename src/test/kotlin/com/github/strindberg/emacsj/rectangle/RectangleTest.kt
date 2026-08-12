package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.ide.CopyPasteManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val FILE = "rectanglefile.txt"

class RectangleTest : EmacsJTestCase() {

    @Test
    fun `copy works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Copy works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Copy works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |bar
                |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |foo
                |FOObar
                |BARbaz<caret>BAZ
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>bar
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |bar
                |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |foobar
                |barbaz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>
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
            """
                |foo
                |barbaz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 06`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>foo
                |
                |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |foo
                |
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |foo
                |
                |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 07`() {
        myFixture.configureByText(
            FILE,
            """
                |a<selection>foo
                |b
                |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |foo
                |
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |afoo
                |b
                |cbar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 08`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>fooa
                |
                |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |foo
                |
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |fooa
                |
                |bar<caret>c
            """.trimMargin()
        )
    }

    @Test
    fun `Copy works 09`() {
        myFixture.configureByText(
            FILE,
            """
                |   <selection>foo
                |      
                |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(
            """
                |foo
                |   
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |   foo
                |      
                |   bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `Cut works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals("foo", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString())
        myFixture.checkResult("<caret>")
    }

    @Test
    fun `Cut works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |bar
                |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |foo
                |FOO
                |BAR<caret>BAZ
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>bar
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |bar
                |baz
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |foo
                |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>
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
            """
                |foo
                |bar<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 06`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>foo
                |
                |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |foo
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
    fun `Cut works 07`() {
        myFixture.configureByText(
            FILE,
            """
                |a<selection>foo
                |b
                |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |foo
                |
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |a
                |b
                |c<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 08`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>fooa
                |
                |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |foo
                |
                |bar
            """.trimMargin(),
            CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor).toString()
        )
        myFixture.checkResult(
            """
                |a
                |
                |<caret>c
            """.trimMargin()
        )
    }

    @Test
    fun `Cut works 09`() {
        myFixture.configureByText(
            FILE,
            """
                |   <selection>foo
                |      
                |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CUT_RECTANGLE)

        assertEquals(
            """
                |foo
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
    fun `Open works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    @Test
    fun `Open works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult("<caret>   foo")
    }

    @Test
    fun `Open works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |foo
                |FOO<caret>   bar
                |BAR   bazBAZ
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>bar
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |foo<caret>   bar
                |bar   baz
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |foo<caret>
                |bar   baz
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 06`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>foo
                |
                |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |<caret>   foo
                |
                |   bar
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 07`() {
        myFixture.configureByText(
            FILE,
            """
                |a<selection>foo
                |b
                |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |a<caret>   foo
                |b
                |c   bar
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 08`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>fooa
                |
                |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |<caret>   fooa
                |
                |   barc
            """.trimMargin()
        )
    }

    @Test
    fun `Open works 09`() {
        myFixture.configureByText(
            FILE,
            """
                |   <selection>foo
                |      
                |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_OPEN_RECTANGLE)

        myFixture.checkResult(
            """
                |   <caret>   foo
                |         
                |      bar
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 01`() {
        myFixture.configureByText(FILE, "<selection>foo</selection><caret>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    @Test
    fun `Clear works 02`() {
        myFixture.configureByText(FILE, "<selection><caret>foo</selection>")

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult("<caret>   ")
    }

    @Test
    fun `Clear works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |foo
                |FOO<caret>   
                |BAR   BAZ
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>bar
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |foo<caret>   
                |bar   
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |foo<selection>
                |barbaz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |foo<caret>
                |bar   
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 06`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>foo
                |
                |bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |<caret>   
                |
                |   
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 07`() {
        myFixture.configureByText(
            FILE,
            """
                |a<selection>foo
                |b
                |cbar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |a<caret>   
                |b
                |c   
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 08`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>fooa
                |
                |bar</selection><caret>c
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |<caret>   a
                |
                |   c
            """.trimMargin()
        )
    }

    @Test
    fun `Clear works 09`() {
        myFixture.configureByText(
            FILE,
            """
                |   <selection>foo
                |      
                |   bar</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CLEAR_RECTANGLE)

        myFixture.checkResult(
            """
                |   <caret>   
                |      
                |      
            """.trimMargin()
        )
    }

    @Test
    fun `Keep works 01`() {
        myFixture.configureByText(FILE, "bar<selection>foo</selection><caret>baz")

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Keep works 02`() {
        myFixture.configureByText(FILE, "bar<caret><selection>foo</selection>baz")

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Keep works 03`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """
                |foo
                |bar
                |baz<caret>
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Keep works 04`() {
        myFixture.configureByText(
            FILE,
            """
                |foo
                |FOO<selection>bar
                |BARba
                |BARbaz</selection><caret>BAZ
                |omf
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """
                |foo
                |bar
                |ba
                |baz<caret>
                |omf
            """.trimMargin()
        )
    }

    @Test
    fun `Keep works 05`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>foo
                |bar
                |baz</selection><caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_KEEP_RECTANGLE)

        myFixture.checkResult(
            """
                |foo
                |bar
                |baz<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Rectangle operation reduces multiple carets to one and uses the primary selection`() {
        myFixture.configureByText(
            FILE,
            """
                fooX
                barY
            """.trimIndent()
        )
        myFixture.editor.caretModel.caretsAndSelections = [
            CaretState(LogicalPosition(0, 3), LogicalPosition(0, 0), LogicalPosition(0, 3)),
            CaretState(LogicalPosition(1, 3), LogicalPosition(1, 0), LogicalPosition(1, 3))
        ]
        assertEquals(2, myFixture.editor.caretModel.caretCount)

        myFixture.performEditorAction(ACTION_COPY_RECTANGLE)

        assertEquals(1, myFixture.editor.caretModel.caretCount)
        assertEquals("bar", CopyPasteManager.getInstance().contents?.getTransferData(DataFlavor.stringFlavor))
    }
}
