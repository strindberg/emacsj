package com.github.strindberg.emacsj.word

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor

internal fun Document.substring(start: Int, end: Int) = charsSequence.subSequence(start, end).toString()

internal val Editor.text
    get() = document.charsSequence

internal val Editor.isCamel
    get() = settings.isCamelWords

internal fun currentWordBoundaries(text: CharSequence, offset: Int, isCamel: Boolean, isForward: Boolean): Pair<Int, Int> =
    if (isForward) {
        currentWordStart(text, offset, isCamel).let { wordStart ->
            Pair(wordStart, currentWordEnd(text, wordStart, isCamel))
        }
    } else {
        currentWordEnd(text, offset, isCamel).let { wordEnd ->
            Pair(currentWordStart(text, wordEnd, isCamel), wordEnd)
        }
    }

internal fun currentWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Int {
    val start = if (isWordStart(text, offset, isCamel)) offset - 1 else offset
    return previousWordStart(text, start, isCamel)
}

internal fun currentWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Int {
    val start = if (isWordEnd(text, offset, isCamel)) offset + 1 else offset
    return nextWordEnd(text, start, isCamel)
}

internal fun nextWordBoundaries(text: CharSequence, offset: Int, isCamel: Boolean): Pair<Int, Int> =
    nextWordStart(text, offset, isCamel).let { wordStart ->
        Pair(wordStart, nextWordEnd(text, wordStart + 1, isCamel))
    }

internal fun previousWordBoundaries(text: CharSequence, offset: Int, isCamel: Boolean): Pair<Int, Int> =
    previousWordEnd(text, offset, isCamel).let { wordEnd ->
        Pair(previousWordStart(text, wordEnd - 1, isCamel), wordEnd)
    }

private fun nextWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Int =
    nextBoundary(text, offset, isCamel, ::isWordStart)

private fun nextWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Int =
    nextBoundary(text, offset, isCamel, ::isWordEnd)

private fun nextBoundary(text: CharSequence, offset: Int, isCamel: Boolean, check: (CharSequence, Int, Boolean) -> Boolean): Int {
    tailrec fun next(offset: Int): Int =
        if (offset >= text.length) {
            text.length
        } else if (check(text, offset, isCamel)) {
            offset
        } else {
            next(offset + 1)
        }
    return next(offset)
}

private fun previousWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Int =
    previousBoundary(text, offset, isCamel, ::isWordStart)

private fun previousWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Int =
    previousBoundary(text, offset, isCamel, ::isWordEnd)

private fun previousBoundary(text: CharSequence, offset: Int, isCamel: Boolean, check: (CharSequence, Int, Boolean) -> Boolean): Int {
    tailrec fun previous(offset: Int): Int =
        if (offset <= 0) {
            0
        } else if (check(text, offset, isCamel)) {
            offset
        } else {
            previous(offset - 1)
        }
    return previous(offset)
}

private fun isWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Boolean =
    (offset > 0 && offset < text.length) && run {
        val previous = text[offset - 1]
        val current = text[offset]
        current.isLetterOrDigit() && (!previous.isLetterOrDigit() || isCamelBoundary(previous, current, isCamel))
    }

private fun isWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Boolean =
    (offset > 0 && offset < text.length) && run {
        val previous = text[offset - 1]
        val current = text[offset]
        previous.isLetterOrDigit() && (!current.isLetterOrDigit() || isCamelBoundary(previous, current, isCamel))
    }

private fun isCamelBoundary(prev: Char, curr: Char, isCamel: Boolean): Boolean =
    isCamel && prev.isLowerCaseOrDigit() && curr.isUpperCase()

private fun Char.isLowerCaseOrDigit(): Boolean = isLowerCase() || isDigit()
