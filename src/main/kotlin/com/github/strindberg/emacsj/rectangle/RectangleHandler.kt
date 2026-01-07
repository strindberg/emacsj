package com.github.strindberg.emacsj.rectangle

import java.awt.datatransfer.StringSelection
import com.github.strindberg.emacsj.word.substring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.CopyPasteManager
import org.intellij.lang.annotations.Language

enum class Type { COPY, CUT, OPEN, CLEAR, KEEP }

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

class RectangleHandler(val type: Type) : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, editorCaret: Caret?, dataContext: DataContext) {
        (editor as? EditorEx)?.let {
            val caret = editor.caretModel.primaryCaret

            if (caret.hasSelection()) {
                editor.caretModel.removeSecondaryCarets()

                val startPosition = editor.offsetToVisualPosition(caret.selectionRange.startOffset)
                val endPosition = editor.offsetToVisualPosition(caret.selectionRange.endOffset)
                val minColumn = minOf(startPosition.column, endPosition.column)
                val maxColumn = maxOf(startPosition.column, endPosition.column)

                val buffer = mutableListOf<String>()
                var lastTo: Int = -1
                for (line in startPosition.line..endPosition.line) {
                    val from = minOf(editor.document.getLineStartOffset(line) + minColumn, editor.document.getLineEndOffset(line))
                    val to = minOf(editor.document.getLineStartOffset(line) + maxColumn, editor.document.getLineEndOffset(line))
                    when (type) {
                        Type.COPY -> buffer.add(editor.document.substring(from, to))
                        Type.CUT -> {
                            buffer.add(editor.document.substring(from, to))
                            editor.document.deleteString(from, to)
                        }
                        Type.OPEN -> editor.document.insertString(from, " ".repeat(to - from))
                        Type.CLEAR -> editor.document.replaceString(from, to, " ".repeat(to - from))
                        Type.KEEP -> {
                            val originalEnd = editor.document.getLineEndOffset(line)
                            val deleted = (from - editor.document.getLineStartOffset(line)) + (editor.document.getLineEndOffset(line) - to)
                            editor.document.deleteString(to, editor.document.getLineEndOffset(line))
                            editor.document.deleteString(editor.document.getLineStartOffset(line), from)
                            lastTo = originalEnd - deleted
                        }
                    }
                }

                when (type) {
                    Type.COPY, Type.CUT -> CopyPasteManager.getInstance().setContents(StringSelection(buffer.joinToString("\n")))
                    Type.OPEN, Type.CLEAR -> caret.moveToOffset(editor.visualPositionToOffset(startPosition))
                    Type.KEEP -> if (lastTo != -1) caret.moveToOffset(lastTo)
                }

                caret.removeSelection()
                editor.isStickySelection = false
            }
        }
    }
}
