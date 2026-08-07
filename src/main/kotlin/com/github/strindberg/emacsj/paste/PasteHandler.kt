package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.Transferable
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.paste.PasteType.HISTORY
import com.github.strindberg.emacsj.paste.PasteType.PREFIX
import com.github.strindberg.emacsj.paste.PasteType.STANDARD
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCopyPasteHelper
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import org.intellij.lang.annotations.Language

enum class PasteType { STANDARD, PREFIX, HISTORY }

@Language("devkit-action-id")
internal const val ACTION_PASTE = "com.github.strindberg.emacsj.actions.paste.paste"

@Language("devkit-action-id")
internal const val ACTION_PREFIX_PASTE = "com.github.strindberg.emacsj.actions.paste.pasteprefix"

@Language("devkit-action-id")
internal const val ACTION_HISTORY_PASTE = "com.github.strindberg.emacsj.actions.paste.pastehistory"

private val LAST_PASTED_REGIONS = Key.create<List<TextRange>>("PasteHandler.LAST_PASTED_REGIONS")

private val pasteActionIds = setOf(ACTION_PASTE, ACTION_PREFIX_PASTE, ACTION_HISTORY_PASTE)

private const val CLIPBOARD_HISTORY_SIZE = 64

internal class PasteHandler(private val type: PasteType) : EditorWriteActionHandler() {

    companion object {
        private var clipboardHistory = emptyList<Transferable>()

        private var clipboardHistoryPos = 0

        private var pasteType = STANDARD
    }

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        when (type) {
            STANDARD, PREFIX -> {
                clipboardHistory = clipboardHistory().take(CLIPBOARD_HISTORY_SIZE)
                clipboardHistoryPos = 0

                if (EmacsJService.instance.isLastStrictUniversal()) {
                    pasteType = PREFIX
                    editor.pasteAndMove(0)
                } else {
                    pasteType = type
                    editor.pasteAndMove(EmacsJService.instance.universalArgument() - 1)
                }

                editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            }
            HISTORY -> {
                editor.getUserData(LAST_PASTED_REGIONS)?.let { regions ->
                    if (EmacsJService.instance.lastActionId() in pasteActionIds) {
                        regions.sortedByDescending { it.startOffset }.forEach { region ->
                            editor.document.deleteString(region.startOffset, region.endOffset)
                        }
                        editor.pasteAndMove()
                    }
                }
            }
        }
    }

    private fun Editor.pasteAndMove(steps: Int = 0) {
        nextHistoryClipboard(steps)?.let { contents ->
            val ranges = pasteTransferable(contents)
            putUserData(LAST_PASTED_REGIONS, ranges)
            ranges.forEach { range ->
                caretModel.allCarets.firstOrNull { it.offset == range.endOffset }?.let { caret ->
                    if (caretModel.allCarets.size == 1) {
                        caret.moveToOffset(if (pasteType == STANDARD) range.startOffset else range.endOffset)
                        MarkHandler.pushPlaceInfo(this)
                    }
                    caret.moveToOffset(if (pasteType == STANDARD) range.endOffset else range.startOffset)
                }
            }
        }
    }

    private fun nextHistoryClipboard(steps: Int): Transferable? =
        clipboardHistory.takeUnless { it.isEmpty() }?.let { history ->
            clipboardHistoryPos += steps
            history[clipboardHistoryPos++ % history.size]
        }

    private fun Editor.pasteTransferable(contents: Transferable): List<TextRange> =
        EditorCopyPasteHelper.getInstance().pasteTransferable(this, contents)?.toList().orEmpty()
}
