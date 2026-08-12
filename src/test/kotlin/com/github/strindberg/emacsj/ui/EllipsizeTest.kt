package com.github.strindberg.emacsj.ui

import java.awt.Font
import java.awt.FontMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

/** Every character ten pixels wide, so the arithmetic in the assertions is readable. */
private val metrics: FontMetrics = object : FontMetrics(Font("Dialog", Font.PLAIN, 12)) {
    override fun stringWidth(str: String): Int = str.length * 10
}

class EllipsizeTest {

    @ParameterizedTest(name = "[{0}] in {1}px is [{2}]")
    @MethodSource("widthCases")
    fun `Text is cut to fit, ellipsis included in the width`(text: String, maxWidth: Int, expected: String) {
        assertEquals(expected, ellipsize(text, maxWidth, metrics))
    }

    @Test
    fun `A width of zero leaves the text untouched rather than erasing it`() {
        // Happens before the list has been laid out; truncating to nothing there would blank every row.
        assertEquals("abcdefghij", ellipsize("abcdefghij", 0, metrics))
    }

    fun widthCases() = [
        // Text that fits is left alone.
        Arguments.of("abcde", 50, "abcde"),
        Arguments.of("abcde", 500, "abcde"),
        // Text that does not fit ends in an ellipsis.
        Arguments.of("abcdefghij", 50, "abcd…"),
        // The ellipsis itself takes room.
        Arguments.of("abcdefghij", 55, "abcd…"),
        Arguments.of("abcdefghij", 60, "abcde…"),
        // A width too small for anything gives just the ellipsis.
        Arguments.of("abcdefghij", 10, "…")
    ]
}
