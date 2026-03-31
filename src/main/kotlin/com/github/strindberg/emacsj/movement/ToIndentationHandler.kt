package com.github.strindberg.emacsj.movement

import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_TO_INDENTATION = "com.github.strindberg.emacsj.actions.movement.toindentation"

class ToIndentationHandler : EditorActionHandler.ForEachCaret() {

    override fun doExecute(editor: Editor, caret: Caret, dataContext: DataContext?) {
        val document = editor.document
        val currentLineNumber = document.getLineNumber(caret.offset)
        val currentLineStart = document.getLineStartOffset(currentLineNumber)
        val currentLineEnd = document.getLineEndOffset(currentLineNumber)
        val offset = firstNonSpaceOffset(document.substring(currentLineStart, currentLineEnd))
        if (offset != -1) {
            caret.moveToOffset(currentLineStart + offset)
        }
    }

    private fun firstNonSpaceOffset(text: CharSequence): Int {
        text.forEachIndexed { index, char ->
            if (!char.isWhitespace()) {
                return index
            }
        }
        return -1
    }
}
