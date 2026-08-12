package com.github.strindberg.emacsj.word

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

private const val CASE_NAME = "offset {1} of [{0}] is [{2}]"

/** The word the caret sits in, for each combination of camel-case and direction. */
class WordUtilsTest {

    @ParameterizedTest(name = CASE_NAME)
    @MethodSource("forwardCases")
    fun `Boundaries forward`(text: String, offset: Int, expected: String) {
        assertEquals(expected, wordAt(text, offset, isCamel = false, isForward = true))
    }

    @ParameterizedTest(name = CASE_NAME)
    @MethodSource("backwardCases")
    fun `Boundaries backward`(text: String, offset: Int, expected: String) {
        assertEquals(expected, wordAt(text, offset, isCamel = false, isForward = false))
    }

    @ParameterizedTest(name = CASE_NAME)
    @MethodSource("camelForwardCases")
    fun `Camel boundaries forward`(text: String, offset: Int, expected: String) {
        assertEquals(expected, wordAt(text, offset, isCamel = true, isForward = true))
    }

    @ParameterizedTest(name = CASE_NAME)
    @MethodSource("camelBackwardCases")
    fun `Camel boundaries backward`(text: String, offset: Int, expected: String) {
        assertEquals(expected, wordAt(text, offset, isCamel = true, isForward = false))
    }

    fun forwardCases() = [
        Arguments.of("ab dc", 0, "ab"),
        Arguments.of("ab dc", 1, "ab"),
        Arguments.of("ab dc", 2, "ab"),
        Arguments.of("ab dc", 3, "ab"),
        Arguments.of("ab dc", 4, "dc"),
        Arguments.of("ab dc", 5, "dc"),
        Arguments.of(" ab dc", 0, " ab"),
        Arguments.of(" ab dc", 1, " ab")
    ]

    fun backwardCases() = [
        Arguments.of("ab dc", 0, "ab"),
        Arguments.of("ab dc", 1, "ab"),
        Arguments.of("ab dc", 2, "dc"),
        Arguments.of("ab dc", 3, "dc"),
        Arguments.of("ab dc", 4, "dc"),
        Arguments.of("ab dc", 5, "dc"),
        Arguments.of("ab dc ", 5, "dc "),
        Arguments.of("ab dc ", 6, "dc ")
    ]

    fun camelForwardCases() = [
        Arguments.of("AbDc", 0, "Ab"),
        Arguments.of("AbDc", 1, "Ab"),
        Arguments.of("AbDc", 2, "Ab"),
        Arguments.of("AbDc", 3, "Dc"),
        Arguments.of("AbDc", 4, "Dc"),
        Arguments.of(" AbDc", 0, " Ab"),
        Arguments.of(" AbDc", 1, " Ab")
    ]

    fun camelBackwardCases() = [
        Arguments.of("AbDc", 0, "Ab"),
        Arguments.of("AbDc", 1, "Ab"),
        Arguments.of("AbDc", 2, "Dc"),
        Arguments.of("AbDc", 4, "Dc"),
        Arguments.of("AbDc ", 4, "Dc "),
        Arguments.of("AbDc ", 5, "Dc ")
    ]

    private fun wordAt(text: String, offset: Int, isCamel: Boolean, isForward: Boolean): String =
        currentWordBoundaries(text = text, offset = offset, isCamel = isCamel, isForward = isForward)
            .let { (start, end) -> text.substring(start, end) }
}
