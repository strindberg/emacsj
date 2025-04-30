package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.kill.Type.COPY
import com.github.strindberg.emacsj.kill.Type.CUT
import com.github.strindberg.emacsj.word.substring
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.KillRingTransferable
import com.intellij.openapi.util.text.StringUtil

private enum class Type { COPY, CUT }

private const val APPEND_NEXT_KILL = "Append Next Kill"

object KillUtil {

    internal fun cut(editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean = false) {
        cutOrCopy(CUT, editor, textStartOffset, textEndOffset, prepend)
    }

    internal fun copy(editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean = false) {
        cutOrCopy(COPY, editor, textStartOffset, textEndOffset, prepend)
    }

    private fun cutOrCopy(type: Type, editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean) {
        if (textStartOffset != textEndOffset) {
            val copyPasteManager = CopyPasteManagerEx.getInstanceEx()
            val previousKill = copyPasteManager.allContents.getOrNull(0)
            val newText = StringUtil.convertLineSeparators(editor.document.substring(textStartOffset, textEndOffset))

            if (EmacsJCommandListener.lastCommandName == APPEND_NEXT_KILL && (previousKill != null)) {
                val previousText = previousKill.getTransferData(DataFlavor.stringFlavor) as String
                copyPasteManager.removeContent(previousKill)
                copyPasteManager.setContents(
                    KillRingTransferable(
                        if (prepend) newText + previousText else previousText + newText,
                        editor.document,
                        textStartOffset,
                        textEndOffset,
                        type == CUT
                    )
                )
            } else {
                copyPasteManager.setContents(KillRingTransferable(newText, editor.document, textStartOffset, textEndOffset, type == CUT))
            }

            if (type == CUT) {
                editor.document.deleteString(textStartOffset, textEndOffset)
            }
        }

        editor.selectionModel.removeSelection()
        if (editor is EditorEx) {
            editor.isStickySelection = false
        }
    }
}
