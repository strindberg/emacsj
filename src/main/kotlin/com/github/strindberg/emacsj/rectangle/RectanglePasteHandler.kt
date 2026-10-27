package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.DataFlavor
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.ide.CopyPasteManager
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_PASTE_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.rectanglepaste"

internal class RectanglePasteHandler : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        val startPosition = primary.logicalPosition

        CopyPasteManager.getInstance().contents?.let { contents ->
            val lines = contents.getTransferData(DataFlavor.stringFlavor).toString().split('\n')
            val maxLength = lines.maxOf { it.length }

            var lastInsert = 0
            lines.forEachIndexed { index, line ->
                val lineNumber = startPosition.line + index
                if (lineNumber >= editor.document.lineCount) {
                    editor.document.insertString(editor.document.textLength, "\n")
                }
                val lineEnd = editor.document.getLineEndOffset(lineNumber)
                val lineWidth = editor.offsetToLogicalPosition(lineEnd).column
                if (lineWidth < startPosition.column) {
                    editor.document.insertString(lineEnd, " ".repeat(startPosition.column - lineWidth))
                }
                // A column inside a tab gives the offset of the tab itself, so the text goes in before it.
                lastInsert = editor.logicalPositionToOffset(LogicalPosition(lineNumber, startPosition.column))
                editor.document.insertString(lastInsert, line.padEnd(maxLength, ' '))
            }

            primary.moveToOffset(lastInsert + maxLength)
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
    }
}
