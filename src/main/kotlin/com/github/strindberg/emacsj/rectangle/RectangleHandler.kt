package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager

enum class Type { COPY, CUT, OPEN, CLEAR }

class RectangleHandler(val type: Type) : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, editorCaret: Caret?, dataContext: DataContext) {
        val ex = editor as EditorEx
        val caret = ex.caretModel.primaryCaret

        if (caret.hasSelection()) {
            ex.caretModel.removeSecondaryCarets()

            val startPosition = ex.offsetToVisualPosition(caret.selectionRange.startOffset)
            val endPosition = ex.offsetToVisualPosition(caret.selectionRange.endOffset)
            val minColumn = minOf(startPosition.column, endPosition.column)
            val maxColumn = maxOf(startPosition.column, endPosition.column)

            val buffer = mutableListOf<String>()
            for (line in startPosition.line..endPosition.line) {
                val from = minOf(ex.document.getLineStartOffset(line) + minColumn, ex.document.getLineEndOffset(line))
                val to = minOf(ex.document.getLineStartOffset(line) + maxColumn, ex.document.getLineEndOffset(line))
                when (type) {
                    Type.COPY -> buffer.add(ex.document.substring(from, to))
                    Type.CUT -> {
                        buffer.add(ex.document.substring(from, to))
                        ex.document.deleteString(from, to)
                    }
                    Type.OPEN -> ex.document.insertString(from, " ".repeat(to - from))
                    Type.CLEAR -> ex.document.replaceString(from, to, " ".repeat(to - from))
                }
            }

            when (type) {
                Type.COPY, Type.CUT -> CopyPasteManager.getInstance().setContents(StringSelection(buffer.joinToString("\n")))
                Type.OPEN, Type.CLEAR -> caret.moveToOffset(ex.visualPositionToOffset(startPosition))
            }

            caret.removeSelection()
            ex.isStickySelection = false
        }
    }
}
