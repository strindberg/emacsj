package com.github.strindberg.emacsj.duplicate

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.util.DocumentUtil
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_COPY_ABOVE_COMMAND = "com.github.strindberg.emacsj.actions.duplicate.copyfromabovecommand"

class CopyFromAboveCommandHandler : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val document = editor.document
        val previousLineNumber = findNonEmptyPrecedingLine(document, document.getLineNumber(caret.offset))
        if (previousLineNumber != null && !caret.hasSelection()) {
            val previousEnd = document.getLineEndOffset(previousLineNumber)
            val column = editor.offsetToVisualPosition(caret.offset).column

            val start = document.getLineStartOffset(previousLineNumber) + column
            val end = if (EmacsJService.instance.isLastUniversal()) start + EmacsJService.instance.universalArgument() else previousEnd

            val copiedString = document.substring(minOf(start, previousEnd), minOf(end, previousEnd))
            document.insertString(caret.offset, copiedString)
            caret.moveToOffset(caret.offset + copiedString.length)
        }
    }

    private fun findNonEmptyPrecedingLine(document: Document, lineNum: Int): Int? {
        tailrec fun findNonEmpty(currentLineNum: Int): Int? =
            if (currentLineNum < 0) {
                null
            } else if (!DocumentUtil.isLineEmpty(document, currentLineNum)) {
                currentLineNum
            } else {
                findNonEmpty(currentLineNum - 1)
            }
        return findNonEmpty(lineNum - 1)
    }
}
