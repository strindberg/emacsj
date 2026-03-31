package com.github.strindberg.emacsj.word

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

class WordTransposeHandler(private val direction: Direction) : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val hasSelection = caret.hasSelection() // This value is false after modification if using sticky selection

        val currentBoundaries = if (hasSelection) {
            Pair(caret.selectionStart, caret.selectionEnd)
        } else {
            currentWordBoundaries(
                text = editor.text,
                offset = caret.offset,
                isCamel = editor.isCamel,
                isForward = direction == Direction.FORWARD
            )
        }
        currentBoundaries.let { (currentStart, currentEnd) ->
            val currentWord = editor.document.substring(currentStart, currentEnd)

            if (direction == Direction.FORWARD) {
                nextWordBoundaries(text = editor.text, offset = currentEnd, isCamel = editor.isCamel).let { (nextStart, nextEnd) ->
                    val nextWord = editor.document.substring(nextStart, nextEnd)

                    editor.document.replaceWords(
                        firstStart = currentStart,
                        firstEnd = currentEnd,
                        secondStart = nextStart,
                        secondEnd = nextEnd,
                        words = lowerCamelCase(
                            caret = caret,
                            firstEnd = currentEnd,
                            secondStart = nextStart,
                            firstWord = currentWord,
                            secondWord = nextWord
                        )
                    )

                    caret.moveToOffset(nextEnd)

                    if (hasSelection) {
                        caret.setSelection(nextEnd - (currentEnd - currentStart), nextEnd)
                    }
                }
            } else {
                previousWordBoundaries(text = editor.text, offset = currentStart, isCamel = editor.isCamel).let { (prevStart, prevEnd) ->
                    val prevWord = editor.document.substring(prevStart, prevEnd)

                    editor.document.replaceWords(
                        firstStart = prevStart,
                        firstEnd = prevEnd,
                        secondStart = currentStart,
                        secondEnd = currentEnd,
                        words = lowerCamelCase(
                            caret = caret,
                            firstEnd = prevEnd,
                            secondStart = currentStart,
                            firstWord = prevWord,
                            secondWord = currentWord
                        )
                    )

                    caret.moveToOffset(prevStart)

                    if (hasSelection) {
                        caret.setSelection(prevStart, prevStart + (currentEnd - currentStart))
                    }
                }
            }
        }
    }

    private fun Document.replaceWords(firstStart: Int, firstEnd: Int, secondStart: Int, secondEnd: Int, words: Pair<String, String>) {
        replaceString(secondStart, secondEnd, words.first)
        replaceString(firstStart, firstEnd, words.second)
    }

    private fun lowerCamelCase(caret: Caret, firstEnd: Int, secondStart: Int, firstWord: String, secondWord: String) =
        if (!caret.hasSelection() &&
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
