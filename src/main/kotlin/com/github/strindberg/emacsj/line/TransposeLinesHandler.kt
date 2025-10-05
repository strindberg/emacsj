package com.github.strindberg.emacsj.line

import com.github.strindberg.emacsj.word.substring
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
        val lineNum = document.getLineNumber(caret.offset)

        if (lineNum > 0) {
            val previousLineStart = document.getLineStartOffset(lineNum - 1)
            val currentLineStart = document.getLineStartOffset(lineNum)
            val currentLineEnd = document.getLineEndOffset(lineNum)

            val currentLine = document.substring(currentLineStart, currentLineEnd)
            document.deleteString(currentLineStart, currentLineEnd + 1)
            document.insertString(previousLineStart, currentLine + "\n")
        }
    }
}
