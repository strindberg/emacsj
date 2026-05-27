package com.github.strindberg.emacsj.word

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import org.intellij.lang.annotations.Language

enum class Direction { FORWARD, BACKWARD }

@Language("devkit-action-id")
internal const val ACTION_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewords"

@Language("devkit-action-id")
internal const val ACTION_REVERSE_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewordsreverse"

class TransposeWordHandler(private val direction: Direction) : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val hasSelection = caret.hasSelection() // This value is false after modification if using sticky selection

        val originalRange =
            if (hasSelection) {
                Pair(caret.selectionStart, caret.selectionEnd)
            } else {
                currentWordBoundaries(
                    text = editor.text,
                    offset = caret.offset,
                    isCamel = editor.isCamel,
                    isForward = direction == Direction.FORWARD,
                )
            }
        val (originalStart, originalEnd) = originalRange

        val replacementRange =
            if (EmacsJService.instance.universalArgument() == 0) {
                MarkHandler.peek(editor)?.let { mark ->
                    currentWordBoundaries(
                        text = editor.text,
                        offset = mark.caretPosition,
                        isCamel = editor.isCamel,
                        isForward = true,
                    )
                }
            } else if (direction == Direction.FORWARD) {
                nextWordBoundaries(editor.text, originalEnd, editor.isCamel)
            } else {
                previousWordBoundaries(editor.text, originalStart, editor.isCamel)
            }

        replacementRange?.let { (replacementStart, replacementEnd) ->
            editor.document.replaceWords(hasSelection, originalRange, replacementRange)

            if (originalStart <= replacementStart) {
                caret.moveToOffset(replacementEnd)
                if (hasSelection) {
                    caret.setSelection(replacementEnd - (originalEnd - originalStart), replacementEnd)
                }
            } else {
                caret.moveToOffset(replacementStart)
                if (hasSelection) {
                    caret.setSelection(replacementStart, replacementStart + (originalEnd - originalStart))
                }
            }
        }
    }

    private fun Document.replaceWords(hasSelection: Boolean, originalRange: Pair<Int, Int>, replacementRange: Pair<Int, Int>) {
        val (firstRange, secondRange) =
            if (originalRange.first <= replacementRange.first) {
                Pair(originalRange, replacementRange)
            } else {
                Pair(replacementRange, originalRange)
            }

        val (firstWord, secondWord) = lowerCamelCase(
            hasSelection = hasSelection,
            firstEnd = firstRange.second,
            secondStart = secondRange.first,
            firstWord = substring(firstRange.first, firstRange.second),
            secondWord = substring(secondRange.first, secondRange.second),
        )
        replaceString(secondRange.first, secondRange.second, firstWord)
        replaceString(firstRange.first, firstRange.second, secondWord)
    }

    private fun lowerCamelCase(hasSelection: Boolean, firstEnd: Int, secondStart: Int, firstWord: String, secondWord: String) =
        if (!hasSelection &&
            firstEnd == secondStart &&
            firstWord.isNotEmpty() &&
            firstWord[0].isLowerCase() &&
            secondWord.isNotEmpty() &&
            secondWord[0].isUpperCase()
        ) {
            Pair(firstWord.replaceFirstChar { it.titlecaseChar() }, secondWord.replaceFirstChar { it.lowercase() })
        } else {
            Pair(firstWord, secondWord)
        }
}
