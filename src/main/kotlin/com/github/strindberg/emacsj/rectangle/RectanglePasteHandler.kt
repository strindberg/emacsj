package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.ide.CopyPasteManager

class RectanglePasteHandler : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        val startPosition = primary.visualPosition

        CopyPasteManager.getInstance().contents?.let { contents ->
            val lines = contents.getTransferData(DataFlavor.stringFlavor).toString().split('\n')
            val maxLength = lines.maxOf { it.length }

            lines.forEachIndexed { index, line ->
                val (lineStart, lineEnd) =
                    if (startPosition.line + index >= editor.document.lineCount) {
                        editor.document.insertString(editor.document.textLength, "\n")
                        Pair(editor.document.textLength, editor.document.textLength)
                    } else {
                        Pair(
                            editor.document.getLineStartOffset(startPosition.line + index),
                            editor.document.getLineEndOffset(startPosition.line + index)
                        )
                    }

                if (lineEnd - lineStart < startPosition.column) {
                    editor.document.replaceString(
                        lineStart,
                        lineEnd,
                        editor.document.substring(lineStart, lineEnd).padEnd(startPosition.column)
                    )
                }
                editor.document.insertString(lineStart + startPosition.column, line.padEnd(maxLength, ' '))
            }

            val newPosition = VisualPosition(startPosition.line + lines.size - 1, startPosition.column + maxLength)
            primary.moveToOffset(editor.visualPositionToOffset(newPosition))
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
    }
}
