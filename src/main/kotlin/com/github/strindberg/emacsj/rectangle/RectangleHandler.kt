package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager
import org.intellij.lang.annotations.Language

enum class RectangleType { COPY, CUT, OPEN, CLEAR, KEEP }

@Language("devkit-action-id")
internal const val ACTION_COPY_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.copyrectangle"

@Language("devkit-action-id")
internal const val ACTION_CUT_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.cutrectangle"

@Language("devkit-action-id")
internal const val ACTION_OPEN_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.openrectangle"

@Language("devkit-action-id")
internal const val ACTION_CLEAR_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.clearrectangle"

@Language("devkit-action-id")
internal const val ACTION_KEEP_RECTANGLE = "com.github.strindberg.emacsj.actions.rectangle.keeprectangle"

internal class RectangleHandler(private val type: RectangleType) : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (editor is EditorEx) {
            // A rectangle is defined by one selection, so this always acts on the primary caret.
            val primary = editor.caretModel.primaryCaret

            if (primary.hasSelection()) {
                editor.caretModel.removeSecondaryCarets()

                val startPosition = editor.offsetToLogicalPosition(primary.selectionRange.startOffset)
                val endPosition = editor.offsetToLogicalPosition(primary.selectionRange.endOffset)
                val minColumn = minOf(startPosition.column, endPosition.column)
                val maxColumn = maxOf(startPosition.column, endPosition.column)

                val buffer: MutableList<String> = []
                var lastTo: Int = -1
                for (line in startPosition.line..endPosition.line) {
                    val from = editor.columnToOffset(line, minColumn)
                    val to = editor.columnToOffset(line, maxColumn)
                    // The columns covered on this line, which outnumber its characters when they include a tab.
                    val width = editor.offsetToLogicalPosition(to).column - editor.offsetToLogicalPosition(from).column
                    when (type) {
                        RectangleType.COPY -> {
                            buffer.add(editor.document.substring(from, to))
                        }
                        RectangleType.CUT -> {
                            buffer.add(editor.document.substring(from, to))
                            editor.document.deleteString(from, to)
                        }
                        RectangleType.OPEN -> {
                            editor.document.insertString(from, " ".repeat(width))
                        }
                        RectangleType.CLEAR -> {
                            editor.document.replaceString(from, to, " ".repeat(width))
                        }
                        RectangleType.KEEP -> {
                            val originalStart = editor.document.getLineStartOffset(line)
                            val originalEnd = editor.document.getLineEndOffset(line)
                            lastTo = originalEnd - ((from - originalStart) + (originalEnd - to))

                            editor.document.deleteString(to, originalEnd)
                            editor.document.deleteString(originalStart, from)
                        }
                    }
                }

                when (type) {
                    RectangleType.COPY, RectangleType.CUT -> CopyPasteManager.getInstance().setContents(
                        StringSelection(buffer.joinToString("\n"))
                    )
                    RectangleType.OPEN, RectangleType.CLEAR -> primary.moveToOffset(editor.logicalPositionToOffset(startPosition))
                    RectangleType.KEEP -> if (lastTo != -1) primary.moveToOffset(lastTo)
                }

                primary.removeSelection()
                editor.isStickySelection = false
            }
        }
    }

    /**
     * The offset at [column] on [line], or the line end on a line too short to reach it. A column inside a tab gives
     * the offset of the tab itself.
     */
    private fun Editor.columnToOffset(line: Int, column: Int): Int =
        minOf(logicalPositionToOffset(LogicalPosition(line, column)), document.getLineEndOffset(line))
}
