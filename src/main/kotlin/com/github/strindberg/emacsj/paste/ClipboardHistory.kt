package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange

internal const val CLIPBOARD_HISTORY_SIZE = 64

internal fun clipboardHistory(): List<Transferable> =
    CopyPasteManager.getInstance().allContents
        .filter { it.isDataFlavorSupported(DataFlavor.stringFlavor) && it.asText().isNotBlank() }
        .distinctBy { it.asText() }

internal fun clipboardHistoryTexts(): List<String> = clipboardHistory().map { it.asText() }

internal fun Transferable.asText(): String = getTransferData(DataFlavor.stringFlavor) as String

/**
 * A walk through the clipboard history: repeated paste-history commands step through it, each step first removing
 * what the previous one inserted.
 */
internal class ClipboardHistory {

    private var history: List<Transferable> = []

    private var position = 0

    /** The regions the last step inserted, which the next step has to remove first. */
    var lastPasted: List<TextRange> = []
        private set

    /** Whether a walk is in progress, i.e. whether there is anything for the next step to replace. */
    val canContinue: Boolean
        get() = lastPasted.isNotEmpty()

    /** Takes a fresh snapshot of the history and abandons any walk in progress. */
    fun restart() {
        history = clipboardHistory().take(CLIPBOARD_HISTORY_SIZE)
        position = 0
        lastPasted = []
    }

    /** The next entry, [steps] further along than the one that would otherwise come, or null when there is none. */
    fun next(steps: Int = 0): Transferable? =
        history.takeUnless { it.isEmpty() }?.let { entries ->
            position += steps
            entries[position++ % entries.size]
        }

    fun record(pasted: List<TextRange>) {
        lastPasted = pasted
    }
}
