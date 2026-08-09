package com.github.strindberg.emacsj.ui

import java.awt.Font
import java.awt.FontMetrics
import junit.framework.TestCase

/** Every character ten pixels wide, so the arithmetic in the assertions is readable. */
private val metrics: FontMetrics = object : FontMetrics(Font("Dialog", Font.PLAIN, 12)) {
    override fun stringWidth(str: String): Int = str.length * 10
}

class EllipsizeTest : TestCase() {

    fun `test Text that fits is left alone`() {
        assertEquals("abcde", ellipsize("abcde", 50, metrics))
        assertEquals("abcde", ellipsize("abcde", 500, metrics))
    }

    fun `test Text that does not fit ends in an ellipsis`() {
        assertEquals("abcd…", ellipsize("abcdefghij", 50, metrics))
    }

    fun `test The ellipsis itself takes room`() {
        assertEquals("abcd…", ellipsize("abcdefghij", 55, metrics))
        assertEquals("abcde…", ellipsize("abcdefghij", 60, metrics))
    }

    fun `test A width too small for anything gives just the ellipsis`() {
        assertEquals("…", ellipsize("abcdefghij", 10, metrics))
    }

    fun `test A width of zero leaves the text untouched rather than erasing it`() {
        // Happens before the list has been laid out; truncating to nothing there would blank every row.
        assertEquals("abcdefghij", ellipsize("abcdefghij", 0, metrics))
    }
}
