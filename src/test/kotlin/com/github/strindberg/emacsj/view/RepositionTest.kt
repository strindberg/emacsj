package com.github.strindberg.emacsj.view

import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_UP
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.EditorTestUtil

private const val FILE = "repositionfile.txt"

private const val LINE_COUNT = 60

private const val VISIBLE_LINES = 20

private const val CARET_LINE = 30

class RepositionTest : EmacsJTestCase() {

    private val caretLine: Int
        get() = myFixture.editor.caretModel.logicalPosition.line

    fun `test Repeated reposition cycles through middle, top and bottom`() {
        configure()

        myFixture.performEditorAction(ACTION_REPOSITION)
        val middle = caretLine
        myFixture.performEditorAction(ACTION_REPOSITION)
        val top = caretLine
        myFixture.performEditorAction(ACTION_REPOSITION)
        val bottom = caretLine

        assertTrue("Second reposition should move the caret above the middle", top < middle)
        assertTrue("Third reposition should move the caret below the middle", bottom > middle)
    }

    fun `test Reposition cycle returns to middle on the fourth invocation`() {
        configure()

        myFixture.performEditorAction(ACTION_REPOSITION)
        val middle = caretLine
        repeat(3) { myFixture.performEditorAction(ACTION_REPOSITION) }

        assertEquals(middle, caretLine)
    }

    fun `test An intervening command restarts the cycle at middle`() {
        configure()

        myFixture.performEditorAction(ACTION_REPOSITION)
        val middle = caretLine
        myFixture.performEditorAction(ACTION_REPOSITION)
        assertTrue(caretLine != middle)

        // Two movements that cancel out, but make reposition no longer the previous command.
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_UP)

        myFixture.performEditorAction(ACTION_REPOSITION)

        assertEquals(middle, caretLine)
    }

    fun `test Reposition moves the caret without scrolling the view`() {
        configure()
        val scrollBefore = myFixture.editor.scrollingModel.verticalScrollOffset

        repeat(3) { myFixture.performEditorAction(ACTION_REPOSITION) }

        assertEquals(scrollBefore, myFixture.editor.scrollingModel.verticalScrollOffset)
    }

    fun `test Caret stays within the visible area at each step of the cycle`() {
        configure()

        repeat(4) {
            myFixture.performEditorAction(ACTION_REPOSITION)
            val caretY = myFixture.editor.visualPositionToXY(myFixture.editor.caretModel.primaryCaret.visualPosition).y
            val relativeY = caretY - myFixture.editor.scrollingModel.verticalScrollOffset
            assertTrue("Caret above the viewport: $relativeY", relativeY >= 0)
            assertTrue(
                "Caret below the viewport: $relativeY",
                relativeY <= myFixture.editor.scrollingModel.visibleArea.height
            )
        }
    }

    fun `test Reposition reduces multiple carets to one`() {
        myFixture.configureByText(FILE, (1..LINE_COUNT).joinToString("\n") { "line $it" })
        EditorTestUtil.setEditorVisibleSize(myFixture.editor, 80, VISIBLE_LINES)
        myFixture.editor.caretModel.setCaretsAndSelections(
            listOf(
                CaretState(LogicalPosition(10, 0), null, null),
                CaretState(LogicalPosition(12, 0), null, null)
            )
        )
        assertEquals(2, myFixture.editor.caretModel.caretCount)

        myFixture.performEditorAction(ACTION_REPOSITION)

        assertEquals(1, myFixture.editor.caretModel.caretCount)
    }

    private fun configure(caretLine: Int = CARET_LINE) {
        myFixture.configureByText(FILE, (1..LINE_COUNT).joinToString("\n") { "line $it" })
        EditorTestUtil.setEditorVisibleSize(myFixture.editor, 80, VISIBLE_LINES)
        myFixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(caretLine, 0))
        // Scroll so that the caret is somewhere in the middle of the viewport rather than at the document start.
        myFixture.performEditorAction(ACTION_RECENTER)
    }
}
