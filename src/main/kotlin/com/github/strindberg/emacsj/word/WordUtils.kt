package com.github.strindberg.emacsj.word

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor

internal fun Document.substring(start: Int, end: Int) = charsSequence.subSequence(start, end).toString()

internal val Editor.text
    get() = document.charsSequence

private val Editor.isCamel
    get() = settings.isCamelWords

internal fun currentWordBoundaries(editor: Editor, caret: Caret): Pair<Int, Int>? =
    if (caret.hasSelection()) {
        Pair(caret.selectionStart, caret.selectionEnd)
    } else {
        currentWordStart(editor, caret.offset)?.let { wordStart ->
            currentWordEnd(editor, wordStart)?.let { wordEnd ->
                Pair(wordStart, wordEnd)
            }
        }
    }

internal fun currentWordStart(editor: Editor, offset: Int): Int? {
    val start = if (isWordStart(editor.text, offset, editor.isCamel)) offset - 1 else offset
    return previousWordStart(editor.text, start, editor.isCamel)
}

internal fun currentWordEnd(editor: Editor, offset: Int): Int? {
    val start = if (isWordEnd(editor.text, offset, editor.isCamel)) offset + 1 else offset
    return nextWordEnd(editor.text, start, editor.isCamel)
}

internal fun nextWordBoundaries(editor: Editor, offset: Int): Pair<Int, Int>? =
    nextWordStart(editor.text, offset, editor.isCamel)?.let { wordStart ->
        Pair(wordStart, nextWordEnd(editor.text, wordStart + 1, editor.isCamel) ?: editor.text.length)
    }

internal fun previousWordBoundaries(editor: Editor, offset: Int): Pair<Int, Int>? =
    previousWordEnd(editor.text, offset, editor.isCamel)?.let { wordEnd ->
        Pair(previousWordStart(editor.text, wordEnd - 1, editor.isCamel) ?: 0, wordEnd)
    }

private fun nextWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Int? =
    nextBoundary(text, offset, isCamel, ::isWordStart)

private fun nextWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Int? =
    nextBoundary(text, offset, isCamel, ::isWordEnd)

private fun nextBoundary(text: CharSequence, offset: Int, isCamel: Boolean, check: (CharSequence, Int, Boolean) -> Boolean): Int? {
    tailrec fun next(offset: Int): Int =
        if (offset > text.length) text.length else if (check(text, offset, isCamel)) offset else next(offset + 1)
    return if (offset >= text.length) null else next(offset)
}

private fun previousWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Int? =
    previousBoundary(text, offset, isCamel, ::isWordStart)

private fun previousWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Int? =
    previousBoundary(text, offset, isCamel, ::isWordEnd)

private fun previousBoundary(text: CharSequence, offset: Int, isCamel: Boolean, check: (CharSequence, Int, Boolean) -> Boolean): Int? {
    tailrec fun previous(offset: Int): Int =
        if (offset < 0) 0 else if (check(text, offset, isCamel)) offset else previous(offset - 1)
    return if (offset <= 0) null else previous(offset)
}

private fun isWordStart(text: CharSequence, offset: Int, isCamel: Boolean): Boolean =
    (offset > 0 && offset < text.length) && isStartBoundary(text[offset - 1], text[offset], text.getOrNull(offset + 1), isCamel)

private fun isWordEnd(text: CharSequence, offset: Int, isCamel: Boolean): Boolean =
    (offset > 0 && offset < text.length) && isEndBoundary(text[offset - 1], text[offset], text.getOrNull(offset + 1), isCamel)

private fun isStartBoundary(previous: Char, current: Char, next: Char?, isCamel: Boolean): Boolean =
    current.isLetterOrDigit() && (!previous.isLetterOrDigit() || isCamelBoundary(previous, current, next, isCamel))

private fun isEndBoundary(previous: Char, current: Char, next: Char?, isCamel: Boolean): Boolean =
    previous.isLetterOrDigit() && (!current.isLetterOrDigit() || isCamelBoundary(previous, current, next, isCamel))

private fun isCamelBoundary(prev: Char, curr: Char, next: Char?, isCamel: Boolean): Boolean =
    isCamel &&
        (prev.isLowerCaseOrDigit() && curr.isUpperCase()) ||
        (prev.isUpperCase() && curr.isUpperCase() && next?.isLowerCaseOrDigit() == true)

private fun Char.isLowerCaseOrDigit(): Boolean = isLowerCase() || isDigit()
