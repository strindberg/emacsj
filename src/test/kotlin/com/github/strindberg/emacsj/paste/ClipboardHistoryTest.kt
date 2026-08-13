package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClipboardHistoryTest : EmacsJTestCase() {

    @Test
    fun `The history steps back through the history and wraps around`() {
        resetClipboard("oldest", "middle", "newest")
        val history = ClipboardHistory()

        history.restart()

        assertEquals(["newest", "middle", "oldest", "newest"], List(4) { history.next()?.asText() })
    }

    @Test
    fun `Steps skip ahead in the history`() {
        resetClipboard("oldest", "middle", "newest")
        val history = ClipboardHistory()

        history.restart()

        assertEquals("oldest", history.next(steps = 2)?.asText())
    }

    @Test
    fun `An empty history has nothing to offer`() {
        resetClipboard()
        val history = ClipboardHistory()

        history.restart()

        assertNull(history.next())
    }

    @Test
    fun `A history continues only once a step has been recorded, and restarting abandons it`() {
        resetClipboard("older", "newer")
        val history = ClipboardHistory()
        history.restart()

        assertFalse(history.canContinue)

        history.record([TextRange(0, 3)])

        assertTrue(history.canContinue)

        history.restart()

        assertFalse(history.canContinue)
    }

    private fun resetClipboard(vararg items: String) {
        (CopyPasteManager.getInstance() as CopyPasteManagerEx).let { manager ->
            manager.allContents.forEach { manager.removeContent(it) }
            items.forEach { manager.setContents(StringSelection(it)) }
        }
    }
}
