package com.github.strindberg.emacsj.view

import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_UP
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.EditorTestUtil

private const val FILE = "recenterfile.txt"

private const val LINE_COUNT = 60

private const val VISIBLE_LINES = 20

private const val CARET_LINE = 30

/**
 * Assertions here are deliberately expressed as relationships between scroll offsets rather than as pixel values:
 * line height depends on the font the test JVM resolves, so concrete offsets would differ between machines.
 */
class RecenterTest : EmacsJTestCase() {

    private val scrollOffset: Int
        get() = myFixture.editor.scrollingModel.verticalScrollOffset

    fun `test Recenter scrolls the view`() {
        configure()
        assertEquals(0, scrollOffset)

        myFixture.performEditorAction(ACTION_RECENTER)

        assertTrue("Recenter should have scrolled the view", scrollOffset > 0)
    }

    fun `test Repeated recenter cycles through middle, top and bottom`() {
        configure()

        myFixture.performEditorAction(ACTION_RECENTER)
        val middle = scrollOffset
        myFixture.performEditorAction(ACTION_RECENTER)
        val top = scrollOffset
        myFixture.performEditorAction(ACTION_RECENTER)
        val bottom = scrollOffset

        // Scrolling further down puts the caret higher on screen, so top > middle > bottom.
        assertTrue("Second recenter should place the caret above the middle", top > middle)
        assertTrue("Third recenter should place the caret below the middle", bottom < middle)
    }

    fun `test Recenter cycle returns to middle on the fourth invocation`() {
        configure()

        myFixture.performEditorAction(ACTION_RECENTER)
        val middle = scrollOffset
        repeat(3) { myFixture.performEditorAction(ACTION_RECENTER) }

        assertEquals(middle, scrollOffset)
    }

    fun `test Typing restarts the cycle at middle`() {
        configure()

        myFixture.performEditorAction(ACTION_RECENTER)
        val middle = scrollOffset
        myFixture.performEditorAction(ACTION_RECENTER)
        assertTrue(scrollOffset != middle)

        myFixture.type("x")

        myFixture.performEditorAction(ACTION_RECENTER)

        assertEquals(middle, scrollOffset)
    }

    fun `test An intervening command restarts the cycle at middle`() {
        configure()

        myFixture.performEditorAction(ACTION_RECENTER)
        val middle = scrollOffset
        myFixture.performEditorAction(ACTION_RECENTER)
        assertTrue(scrollOffset != middle)

        // Two movements that leave the caret where it started, but make recenter no longer the previous command.
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_UP)

        myFixture.performEditorAction(ACTION_RECENTER)

        assertEquals(middle, scrollOffset)
    }

    fun `test Caret stays within the visible area after each step of the cycle`() {
        configure()

        repeat(4) {
            myFixture.performEditorAction(ACTION_RECENTER)
            val caretY = myFixture.editor.visualPositionToXY(myFixture.editor.caretModel.primaryCaret.visualPosition).y
            val relativeY = caretY - scrollOffset
            assertTrue("Caret above the viewport: $relativeY", relativeY >= 0)
            assertTrue(
                "Caret below the viewport: $relativeY",
                relativeY <= myFixture.editor.scrollingModel.visibleArea.height
            )
        }
    }

    private fun configure() {
        myFixture.configureByText(FILE, (1..LINE_COUNT).joinToString("\n") { "line $it" })
        EditorTestUtil.setEditorVisibleSize(myFixture.editor, 80, VISIBLE_LINES)
        myFixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(CARET_LINE, 0))
    }
}
