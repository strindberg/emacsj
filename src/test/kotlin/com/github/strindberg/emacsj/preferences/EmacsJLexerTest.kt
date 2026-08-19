package com.github.strindberg.emacsj.preferences

import com.intellij.lexer.Lexer
import com.intellij.psi.tree.IElementType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EmacsJLexerTest {

    @Test
    fun `Text without the search word yields only text tokens`() {
        assertEquals(
            """
                |text ('foo')
                |text (' ')
                |text ('bar')
                |
            """.trimMargin(),
            EmacsJLexer().printTokens("foo bar")
        )
    }

    @Test
    fun `The first occurrence of the search word is primary`() {
        assertEquals(
            """
                |text ('foo')
                |text (' ')
                |primary ('result')
                |text (' ')
                |text ('bar')
                |
            """.trimMargin(),
            EmacsJLexer().printTokens("foo result bar")
        )
    }

    @Test
    fun `Later occurrences of the search word are secondary`() {
        assertEquals(
            """
                |primary ('result')
                |text (' ')
                |text ('and')
                |text (' ')
                |secondary ('result')
                |
            """.trimMargin(),
            EmacsJLexer().printTokens("result and result")
        )
    }

    @Test
    fun `Tokens cover the whole buffer without gaps or overlaps`() {
        val text = "Other results are indicated as a secondary result."

        val tokens = EmacsJLexer().printTokens(text)

        assertEquals(text, Regex("\\('(.*)'\\)").findAll(tokens).joinToString("") { it.groupValues[1] })
    }

    @Test
    fun `A word merely starting with the search word has its stem matched`() {
        assertEquals(
            """
                |primary ('result')
                |text ('s')
                |
            """.trimMargin(),
            EmacsJLexer().printTokens("results")
        )
    }

    @Test
    fun `The settings page preview has exactly one primary match`() {
        val tokens = EmacsJLexer().printTokens(EmacsJColorSettingsPage().demoText).lines()

        assertEquals(1, tokens.count { it.startsWith("primary ") })
        assertTrue(tokens.any { it.startsWith("secondary ") }, "Preview should also show a secondary match")
    }

    @Test
    fun `Asking for the token type repeatedly gives the same answer`() {
        val text = "result and result"
        val lexer = EmacsJLexer()

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)

        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
    }

    @Test
    fun `Restarting a lexer marks the first match as primary again`() {
        val text = "result and result"
        val lexer = EmacsJLexer()

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)

        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
    }

    private fun Lexer.printTokens(text: String): String {
        start(text, 0, text.length)
        return buildString {
            while (tokenType != null) {
                append(printToken(text, tokenType!!, IntRange(tokenStart, tokenEnd)))
                advance()
            }
        }
    }

    private fun printToken(text: String, tokenType: IElementType, range: IntRange) = "$tokenType ('${tokenText(text, range)}')\n"

    private fun tokenText(text: String, range: IntRange) = text.substring(range.first, range.last).replace("\n", "\\n")
}
