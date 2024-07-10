package com.github.strindberg.emacsj.preferences

import com.intellij.lang.Language
import com.intellij.lexer.LexerBase
import com.intellij.lexer.LexerPosition
import com.intellij.psi.tree.IElementType

val PRIMARY_TOKEN_TYPE = IElementType("primary", Language.ANY)
val SECONDARY_TOKEN_TYPE = IElementType("secondary", Language.ANY)
val TEXT_TOKEN_TYPE = IElementType("text", Language.ANY)

const val STATE_WORD = 0
const val STATE_OTHER = 1

const val SEARCH_WORD = "result"

class EmacsJLexer : LexerBase() {

    private lateinit var buffer: CharSequence
    private var currentState = 0
    private var currentOffset = 0
    private var endOffset = 0
    private var foundFirst = false

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        currentOffset = startOffset
        this.endOffset = endOffset
        currentState = getCurrentState()
    }

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = endOffset

    override fun getState(): Int = currentState

    override fun getTokenType(): IElementType? =
        if (currentOffset < endOffset) {
            if (currentState == STATE_OTHER) {
                TEXT_TOKEN_TYPE
            } else {
                val currentToken = buffer.substring(currentOffset, getNextEnd())
                if (currentToken == SEARCH_WORD && !foundFirst) {
                    foundFirst = true
                    PRIMARY_TOKEN_TYPE
                } else if (currentToken == SEARCH_WORD) {
                    SECONDARY_TOKEN_TYPE
                } else {
                    TEXT_TOKEN_TYPE
                }
            }
        } else {
            null
        }

    override fun getTokenStart(): Int = currentOffset

    override fun getTokenEnd(): Int = getNextEnd()

    override fun getCurrentPosition(): LexerPosition = LexerPositionImpl(currentOffset, currentState)

    override fun restore(position: LexerPosition) {
        currentOffset = position.offset
        currentState = position.state
    }

    override fun advance() {
        currentOffset = getNextEnd()
        currentState = getCurrentState()
    }

    private fun getNextEnd(): Int =
        if (currentState == STATE_WORD) {
            if (buffer.substring(currentOffset).startsWith(SEARCH_WORD)) {
                currentOffset + SEARCH_WORD.length
            } else {
                findFirst { !isLetterOrDigit() }
            }
        } else {
            findFirst { isLetterOrDigit() }
        }

    private fun findFirst(predicate: Char.() -> Boolean): Int {
        for ((idx, char) in buffer.withIndex()) {
            if (idx >= currentOffset && char.predicate()) {
                return idx
            }
        }
        return endOffset
    }

    private fun getCurrentState(): Int =
        if (buffer.getOrNull(currentOffset)?.isLetterOrDigit() == false) {
            STATE_OTHER
        } else {
            STATE_WORD
        }

    private data class LexerPositionImpl(val myOffset: Int, val myState: Int) : LexerPosition {
        override fun getOffset(): Int = myOffset
        override fun getState(): Int = myState
    }
}
