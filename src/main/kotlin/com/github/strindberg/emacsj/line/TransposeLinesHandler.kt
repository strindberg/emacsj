package com.github.strindberg.emacsj.line

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_TRANSPOSE_LINES = "com.github.strindberg.emacsj.actions.line.transposelines"

class TransposeLinesHandler : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, editorCaret: Caret?, dataContext: DataContext) {
        val caret = editor.caretModel.primaryCaret
        val document = editor.document
        val currentLineNumber = document.getLineNumber(caret.offset)

        val lastArgument = EmacsJService.instance.universalArgument()
        val replaceLineNumber =
            if (lastArgument == 0) {
                MarkHandler.peek(editor)?.caretPosition?.let { position -> document.getLineNumber(position) } ?: (currentLineNumber - 1)
            } else {
                currentLineNumber - lastArgument
            }

        if (replaceLineNumber >= 0) {
            val replaceLineStart = document.getLineStartOffset(replaceLineNumber)
            val replaceLineEnd = document.getLineEndOffset(replaceLineNumber)
            val currentLineStart = document.getLineStartOffset(currentLineNumber)
            val currentLineEnd = document.getLineEndOffset(currentLineNumber)

            val replaceLine = document.substring(replaceLineStart, replaceLineEnd)
            val currentLine = document.substring(currentLineStart, currentLineEnd)

            document.replaceString(currentLineStart, minOf(editor.text.length, currentLineEnd + 1), replaceLine + "\n")
            document.replaceString(replaceLineStart, replaceLineEnd + 1, currentLine + "\n")

            editor.caretModel.moveToOffset(currentLineEnd + 1)
        }
    }
}
