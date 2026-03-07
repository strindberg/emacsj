package com.github.strindberg.emacsj.kill

import java.awt.datatransfer.DataFlavor
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.kill.Type.COPY
import com.github.strindberg.emacsj.kill.Type.CUT
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.zap.zapCommandNames
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ide.KillRingTransferable
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.VisibleForTesting

private enum class Type { COPY, CUT }

object KillUtil {

    @VisibleForTesting
    internal var testing = false

    private var lastInvocation = OffsetDateTime.MIN

    internal fun cut(editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean = false) {
        cutOrCopy(CUT, editor, textStartOffset, textEndOffset, prepend)
    }

    internal fun copy(editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean = false) {
        cutOrCopy(COPY, editor, textStartOffset, textEndOffset, prepend)
    }

    private fun cutOrCopy(type: Type, editor: Editor, textStartOffset: Int, textEndOffset: Int, prepend: Boolean) {
        editor.selectionModel.removeSelection()
        (editor as? EditorEx)?.isStickySelection = false

        if (textStartOffset != textEndOffset && debounced()) {
            val copyPasteManager = CopyPasteManagerEx.getInstanceEx()
            val previousKill = copyPasteManager.allContents.getOrNull(0)
            val newText = StringUtil.convertLineSeparators(editor.document.substring(textStartOffset, textEndOffset))

            if (appendNextKill() && previousKill != null) {
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
            lastInvocation = now()
        }
    }

    // Avoid inadvertently running the command multiple times because of key repeat.
    private fun debounced(): Boolean = testing || lastInvocation.isBefore(now().minus(80.milliseconds.toJavaDuration()))

    private fun appendNextKill(): Boolean {
        val lastCommandNames = EmacsJService.instance.lastCommandNames()
        return lastCommandNames.last == EmacsJBundle.actionText(ACTION_APPEND_NEXT_KILL) ||
            (lastCommandNames.last in zapCommandNames && lastCommandNames.previous == EmacsJBundle.actionText(ACTION_APPEND_NEXT_KILL))
    }
}
