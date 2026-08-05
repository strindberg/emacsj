package com.github.strindberg.emacsj.preferences

import com.intellij.testFramework.LexerTestCase
import junit.framework.TestCase

class EmacsJLexerTest : TestCase() {

    fun `test Text without the search word yields only text tokens`() {
        assertEquals(
            """
                |text ('foo')
                |text (' ')
                |text ('bar')
                |
            """.trimMargin(),
            LexerTestCase.printTokens("foo bar", 0, EmacsJLexer())
        )
    }

    fun `test The first occurrence of the search word is primary`() {
        assertEquals(
            """
                |text ('foo')
                |text (' ')
                |primary ('result')
                |text (' ')
                |text ('bar')
                |
            """.trimMargin(),
            LexerTestCase.printTokens("foo result bar", 0, EmacsJLexer())
        )
    }

    fun `test Later occurrences of the search word are secondary`() {
        assertEquals(
            """
                |primary ('result')
                |text (' ')
                |text ('and')
                |text (' ')
                |secondary ('result')
                |
            """.trimMargin(),
            LexerTestCase.printTokens("result and result", 0, EmacsJLexer())
        )
    }

    fun `test Tokens cover the whole buffer without gaps or overlaps`() {
        val text = "Other results are indicated as a secondary result."

        val tokens = LexerTestCase.printTokens(text, 0, EmacsJLexer())

        assertEquals(text, Regex("\\('(.*)'\\)").findAll(tokens).joinToString("") { it.groupValues[1] })
    }

    fun `test A word merely starting with the search word has its stem matched`() {
        // Consequence of the prefix test in getNextEnd: "results" is split into "result" plus "s". This shows up in
        // the settings page preview, which contains the word "results".
        assertEquals(
            """
                |primary ('result')
                |text ('s')
                |
            """.trimMargin(),
            LexerTestCase.printTokens("results", 0, EmacsJLexer())
        )
    }

    fun `test The settings page preview has exactly one primary match`() {
        val tokens = LexerTestCase.printTokens(EmacsJColorSettingsPage().demoText, 0, EmacsJLexer()).lines()

        assertEquals(1, tokens.count { it.startsWith("primary ") })
        assertTrue("Preview should also show a secondary match", tokens.any { it.startsWith("secondary ") })
    }

    fun `test Asking for the token type repeatedly gives the same answer`() {
        val text = "result and result"
        val lexer = EmacsJLexer()

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)

        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
    }

    fun `test Restarting a lexer marks the first match as primary again`() {
        val text = "result and result"
        val lexer = EmacsJLexer()

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)
        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)

        lexer.start(buffer = text, startOffset = 0, endOffset = text.length, initialState = 0)

        assertEquals(PRIMARY_TOKEN_TYPE, lexer.tokenType)
    }
}
