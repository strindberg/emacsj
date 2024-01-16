package com.github.strindberg.emacsj.word

import java.util.*
import com.github.strindberg.emacsj.word.ChangeType.CAPITAL
import com.github.strindberg.emacsj.word.ChangeType.CAPITAL_PREVIOUS
import com.github.strindberg.emacsj.word.ChangeType.DELETE
import com.github.strindberg.emacsj.word.ChangeType.DELETE_PREVIOUS
import com.github.strindberg.emacsj.word.ChangeType.LOWER
import com.github.strindberg.emacsj.word.ChangeType.LOWER_PREVIOUS
import com.github.strindberg.emacsj.word.ChangeType.UPPER
import com.github.strindberg.emacsj.word.ChangeType.UPPER_PREVIOUS
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

enum class ChangeType { UPPER, LOWER, CAPITAL, UPPER_PREVIOUS, LOWER_PREVIOUS, CAPITAL_PREVIOUS, DELETE, DELETE_PREVIOUS }

class WordChangeHandler(private val type: ChangeType) : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val (start, end) =
            if (caret.hasSelection()) {
                Pair(caret.selectionStart, caret.selectionEnd)
            } else {
                when (type) {
                    UPPER, LOWER, DELETE -> Pair(caret.offset, currentWordEnd(editor.text, caret.offset, editor.isCamel))
                    CAPITAL -> Pair(
                        firstLetterOrDigit(editor.text, caret.offset),
                        currentWordEnd(editor.text, caret.offset, editor.isCamel)
                    )
                    DELETE_PREVIOUS, UPPER_PREVIOUS, LOWER_PREVIOUS, CAPITAL_PREVIOUS -> Pair(
                        currentWordStart(editor.text, caret.offset, editor.isCamel),
                        caret.offset
                    )
                }
            }

        if (start != null) {
            when (type) {
                DELETE, DELETE_PREVIOUS -> editor.document.deleteString(start, end)
                UPPER, UPPER_PREVIOUS -> replaceTextAndMove(editor.document, start, end, caret) { uppercase() }
                LOWER, LOWER_PREVIOUS -> replaceTextAndMove(editor.document, start, end, caret) { lowercase() }
                CAPITAL, CAPITAL_PREVIOUS -> capitalizeRegion(editor, start, end, caret)
            }

            caret.removeSelection()
        }
    }

    private fun replaceTextAndMove(document: Document, start: Int, end: Int, caret: Caret, operation: String.() -> String) {
        document.replaceString(start, end, document.substring(start, end).operation())
        caret.moveToOffset(end)
    }

    private fun capitalizeRegion(editor: Editor, regionStart: Int, regionEnd: Int, caret: Caret) {
        tailrec fun capitalizeWord(document: Document, start: Int, end: Int) {
            document.replaceString(
                start,
                end,
                document.substring(start, end).lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
            )
            if (end < regionEnd) {
                capitalizeWord(
                    document,
                    firstLetterOrDigit(editor.text, end) ?: regionEnd,
                    currentWordEnd(editor.text, end, editor.isCamel)
                )
            }
        }
        capitalizeWord(editor.document, regionStart, currentWordEnd(editor.text, regionStart, editor.isCamel))
        caret.moveToOffset(regionEnd)
    }

    private fun firstLetterOrDigit(text: CharSequence, offset: Int): Int? {
        tailrec fun next(offset: Int): Int? =
            if (offset >= text.length) {
                null
            } else if (text[offset].isLetterOrDigit()) {
                offset
            } else {
                next(offset + 1)
            }
        return next(offset)
    }
}
