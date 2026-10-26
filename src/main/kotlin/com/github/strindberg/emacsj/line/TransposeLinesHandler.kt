package com.github.strindberg.emacsj.line

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_TRANSPOSE_LINES = "com.github.strindberg.emacsj.actions.line.transposelines"

internal class TransposeLinesHandler : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        // Transposing lines always acts on the primary caret
        val primary = editor.caretModel.primaryCaret
        val document = editor.document
        val currentLineNumber = document.getLineNumber(primary.offset)

        val lastArgument = EmacsJService.instance.universalArgument()
        val replaceLineNumber =
            if (lastArgument == 0) {
                MarkHandler.peek(editor)?.run {
                    document.getLineNumber(caretPosition)
                } ?: (currentLineNumber - 1)
            } else {
                currentLineNumber - lastArgument
            }

        if (replaceLineNumber >= 0) {
            editor.caretModel.removeSecondaryCarets()

            val upperLineNumber = minOf(currentLineNumber, replaceLineNumber)
            val lowerLineNumber = maxOf(currentLineNumber, replaceLineNumber)

            val upperLine = document.lineText(upperLineNumber)
            val lowerLine = document.lineText(lowerLineNumber)

            document.replaceLine(lowerLineNumber, upperLine)
            document.replaceLine(upperLineNumber, lowerLine)

            editor.caretModel.moveToOffset(document.getLineStartOffset(currentLineNumber + 1))
        }
    }

    private fun Document.lineText(lineNumber: Int): String = substring(getLineStartOffset(lineNumber), getLineEndOffset(lineNumber))

    /** Replaces a line together with its line break, giving the last line one if it had none. */
    private fun Document.replaceLine(lineNumber: Int, newText: String) {
        replaceString(getLineStartOffset(lineNumber), minOf(textLength, getLineEndOffset(lineNumber) + 1), newText + "\n")
    }
}
