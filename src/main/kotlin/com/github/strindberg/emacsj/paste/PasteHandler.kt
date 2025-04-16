package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.paste.Type.HISTORY
import com.github.strindberg.emacsj.paste.Type.PREFIX
import com.github.strindberg.emacsj.paste.Type.STANDARD
import com.github.strindberg.emacsj.universal.COMMAND_UNIVERSAL_ARGUMENT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCopyPasteHelper
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange

enum class Type { STANDARD, PREFIX, HISTORY }

internal const val ACTION_PASTE = "com.github.strindberg.emacsj.actions.paste.paste"

private val LAST_PASTED_REGIONS = Key.create<List<TextRange>>("PasteHandler.LAST_PASTED_REGIONS")

private val pasteCommands =
    listOf("Paste: Leave Caret at Point", "Paste: Leave Caret After Pasted Region", "Paste: Previous Item in Clipboard History")

class PasteHandler(val type: Type) : EditorWriteActionHandler() {

    companion object {
        private var clipboardHistory = listOf<Transferable>()

        private var clipboaardHistoryPos = 0

        private var pasteType = STANDARD
    }

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        when (type) {
            STANDARD, PREFIX -> {
                clipboardHistory = filteredContents().take(64)
                clipboaardHistoryPos = 0
                pasteType = if (EmacsJCommandListener.lastCommandName() == COMMAND_UNIVERSAL_ARGUMENT) PREFIX else type
                editor.pasteAndMove()
                editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            }
            HISTORY -> {
                editor.getUserData(LAST_PASTED_REGIONS)?.let { regions ->
                    if (EmacsJCommandListener.lastCommandName() in pasteCommands) {
                        regions.sortedByDescending { it.startOffset }.forEach { region ->
                            editor.document.deleteString(region.startOffset, region.endOffset)
                        }
                        editor.pasteAndMove()
                    }
                }
            }
        }
    }

    private fun Editor.pasteAndMove() {
        nextHistoryClipboard()?.let { contents ->
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

    private fun nextHistoryClipboard(): Transferable? =
        clipboardHistory.takeUnless { it.isEmpty() }?.let { history -> history[clipboaardHistoryPos++ % history.size] }

    private fun Editor.pasteTransferable(contents: Transferable): List<TextRange> =
        EditorCopyPasteHelper.getInstance().pasteTransferable(this, contents)?.toList() ?: emptyList()
}
