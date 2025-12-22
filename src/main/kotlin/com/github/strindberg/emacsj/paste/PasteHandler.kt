package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.paste.Type.HISTORY
import com.github.strindberg.emacsj.paste.Type.PREFIX
import com.github.strindberg.emacsj.paste.Type.STANDARD
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCopyPasteHelper
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import org.intellij.lang.annotations.Language

enum class Type { STANDARD, PREFIX, HISTORY }

@Language("devkit-action-id")
internal const val ACTION_PASTE = "com.github.strindberg.emacsj.actions.paste.paste"

@Language("devkit-action-id")
internal const val ACTION_PREFIX_PASTE = "com.github.strindberg.emacsj.actions.paste.pasteprefix"

@Language("devkit-action-id")
internal const val ACTION_HISTORY_PASTE = "com.github.strindberg.emacsj.actions.paste.pastehistory"

private val LAST_PASTED_REGIONS = Key.create<List<TextRange>>("PasteHandler.LAST_PASTED_REGIONS")

private val pasteCommands =
    listOf(
        EmacsJBundle.actionText(ACTION_PASTE),
        EmacsJBundle.actionText(ACTION_PREFIX_PASTE),
        EmacsJBundle.actionText(ACTION_HISTORY_PASTE)
    )

class PasteHandler(val type: Type) : EditorWriteActionHandler() {

    companion object {
        private var clipboardHistory = listOf<Transferable>()

        private var clipboardHistoryPos = 0

        private var pasteType = STANDARD
    }

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        when (type) {
            STANDARD, PREFIX -> {
                clipboardHistory = filteredContents().take(64)
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
                    if (EmacsJService.instance.lastCommandName() in pasteCommands) {
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

    private fun filteredContents(): List<Transferable> =
        CopyPasteManager.getInstance().allContents
            .filter {
                it.isDataFlavorSupported(DataFlavor.stringFlavor) &&
                    (it.getTransferData(DataFlavor.stringFlavor) as String).isNotBlank()
            }
            .distinctBy { it.getTransferData(DataFlavor.stringFlavor) as String }

    private fun nextHistoryClipboard(steps: Int): Transferable? =
        clipboardHistory.takeUnless { it.isEmpty() }?.let { history ->
            clipboardHistoryPos += steps
            history[clipboardHistoryPos++ % history.size]
        }

    private fun Editor.pasteTransferable(contents: Transferable): List<TextRange> =
        EditorCopyPasteHelper.getInstance().pasteTransferable(this, contents)?.toList().orEmpty()
}
