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
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.impl.EditorCopyPasteHelperImpl
import com.intellij.openapi.util.TextRange
import org.intellij.lang.annotations.Language

enum class PasteType { STANDARD, PREFIX, HISTORY }

@Language("devkit-action-id")
internal const val ACTION_PASTE = "com.github.strindberg.emacsj.actions.paste.paste"

@Language("devkit-action-id")
internal const val ACTION_PREFIX_PASTE = "com.github.strindberg.emacsj.actions.paste.pasteprefix"

@Language("devkit-action-id")
internal const val ACTION_HISTORY_PASTE = "com.github.strindberg.emacsj.actions.paste.pastehistory"

private val pasteActionIds = [ACTION_PASTE, ACTION_PREFIX_PASTE, ACTION_HISTORY_PASTE]

internal class PasteHandler(private val type: PasteType) : EditorWriteActionHandler() {

    companion object {
        private val walk = ClipboardHistory()

        private var pasteType = STANDARD

        internal var killRingDelegate: KillRingDelegate? = null
    }

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        when (type) {
            STANDARD, PREFIX -> {
                walk.restart()

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
                if (walk.canContinue && EmacsJService.instance.lastActionId() in pasteActionIds) {
                    walk.lastPasted.sortedByDescending { it.startOffset }.forEach { region ->
                        editor.document.deleteString(region.startOffset, region.endOffset)
                    }
                    editor.pasteAndMove()
                } else {
                    showKillRing(editor)
                }
            }
        }
    }

    private fun showKillRing(editor: Editor) {
        if (killRingDelegate == null) {
            clipboardHistoryTexts().take(CLIPBOARD_HISTORY_SIZE).takeIf { it.isNotEmpty() }?.let { entries ->
                killRingDelegate = KillRingDelegate(editor, entries, EmacsJService.instance.isLastStrictUniversal())
            }
        }
    }

    private fun Editor.pasteAndMove(steps: Int = 0) {
        walk.next(steps)?.let { contents ->
            val ranges = pasteContents(contents)
            walk.record(ranges)
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

    private fun Editor.pasteContents(contents: Transferable): List<TextRange> =
        if (caretModel.caretCount > 1 && !contents.isWholeLineCopy()) {
            val text = contents.asText()
            EditorModificationUtil.typeInStringAtCaretHonorMultipleCarets(this, text)
            caretModel.allCarets.map { TextRange(it.offset - text.length, it.offset) }
        } else {
            pasteTransferable(contents)
        }

    /** Whether this was copied with no selection, which is what makes the platform paste it as a line of its own. */
    private fun Transferable.isWholeLineCopy(): Boolean =
        runCatching {
            EditorCopyPasteHelperImpl.CopyPasteOptionsTransferableData.valueFromTransferable(this).isCopiedFromEmptySelection
        }.getOrDefault(true)

    private fun Editor.pasteTransferable(contents: Transferable): List<TextRange> =
        EditorCopyPasteHelper.getInstance().pasteTransferable(this, contents)?.toList().orEmpty()
}
