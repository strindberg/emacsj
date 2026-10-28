package com.github.strindberg.emacsj.word

import java.util.Locale
import com.github.strindberg.emacsj.kill.KillUtil
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
import org.intellij.lang.annotations.Language

enum class ChangeType { UPPER, LOWER, CAPITAL, UPPER_PREVIOUS, LOWER_PREVIOUS, CAPITAL_PREVIOUS, DELETE, DELETE_PREVIOUS }

@Language("devkit-action-id")
internal const val ACTION_CAPITAL_CASE = "com.github.strindberg.emacsj.actions.word.capitalcase"

@Language("devkit-action-id")
internal const val ACTION_UPPER_CASE = "com.github.strindberg.emacsj.actions.word.uppercase"

@Language("devkit-action-id")
internal const val ACTION_LOWER_CASE = "com.github.strindberg.emacsj.actions.word.lowercase"

@Language("devkit-action-id")
internal const val ACTION_CAPITAL_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.capitalcaseprevious"

@Language("devkit-action-id")
internal const val ACTION_UPPER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.uppercaseprevious"

@Language("devkit-action-id")
internal const val ACTION_LOWER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.lowercaseprevious"

@Language("devkit-action-id")
internal const val ACTION_DELETE_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.deletenextword"

@Language("devkit-action-id")
internal const val ACTION_DELETE_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.deletepreviousword"

internal class WordChangeHandler(private val type: ChangeType) : EditorWriteActionHandler.ForEachCaret() {

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
                DELETE, DELETE_PREVIOUS -> KillUtil.cut(
                    editor = editor,
                    textStartOffset = start,
                    textEndOffset = end,
                    prepend = type == DELETE_PREVIOUS
                )
                UPPER, UPPER_PREVIOUS -> replaceTextAndMove(
                    document = editor.document,
                    start = start,
                    end = end,
                    caret = caret
                ) { uppercase() }
                LOWER, LOWER_PREVIOUS -> replaceTextAndMove(
                    document = editor.document,
                    start = start,
                    end = end,
                    caret = caret
                ) { lowercase() }
                CAPITAL, CAPITAL_PREVIOUS -> capitalizeRegion(
                    editor = editor,
                    regionStart = start,
                    regionEnd = end,
                    caret = caret
                )
            }

            caret.removeSelection()
        }
    }

    private fun replaceTextAndMove(document: Document, start: Int, end: Int, caret: Caret, operation: String.() -> String) {
        document.replaceString(start, end, document.substring(start, end).operation())
        caret.moveToOffset(end)
    }

    private fun capitalizeRegion(editor: Editor, regionStart: Int, regionEnd: Int, caret: Caret) {
        // Every word is cut off at the region end, and one that only starts after it is left alone.
        tailrec fun capitalizeWord(document: Document, start: Int) {
            val end = minOf(currentWordEnd(editor.text, start, editor.isCamel), regionEnd)
            document.replaceString(
                start,
                end,
                document.substring(start, end).lowercase().replaceFirstChar { it.titlecase(Locale.getDefault()) }
            )
            val next = firstLetterOrDigit(editor.text, end)
            if (next != null && next < regionEnd) {
                capitalizeWord(document, next)
            }
        }
        firstLetterOrDigit(editor.text, regionStart)?.takeIf { it < regionEnd }?.let { capitalizeWord(editor.document, it) }
        caret.moveToOffset(regionEnd)
    }

    private fun firstLetterOrDigit(text: CharSequence, startOffset: Int): Int? {
        tailrec fun next(offset: Int): Int? =
            if (offset >= text.length) {
                null
            } else if (text[offset].isLetterOrDigit()) {
                offset
            } else {
                next(offset + 1)
            }
        return next(startOffset)
    }
}
