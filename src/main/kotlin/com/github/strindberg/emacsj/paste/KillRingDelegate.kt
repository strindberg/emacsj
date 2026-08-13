package com.github.strindberg.emacsj.paste

import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.ui.KillRingUI
import com.github.strindberg.emacsj.ui.UIDelegate
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.ex.util.EditorUtil
import org.jetbrains.annotations.VisibleForTesting

/**
 * The kill ring offered as a list to choose from, shown when paste-history is invoked without a paste to cycle.
 *
 * [asPrefix] mirrors prefix paste: a universal argument ahead of the command leaves the caret where it was, in
 * front of the inserted text, rather than after it.
 */
internal class KillRingDelegate(editor: Editor, private val entries: List<String>, private val asPrefix: Boolean = false) :
    UIDelegate(editor) {

    @VisibleForTesting
    override val ui = KillRingUI(
        editor = editor,
        entries = entries,
        cancelCallback = ::hide,
        keyEventHandler = ::keyEventHandler
    )

    init {
        EditorUtil.disposeWithEditor(editor, this)

        ui.show()
    }

    internal var selectedIndex: Int
        get() = ui.selectedIndex
        set(index) {
            ui.selectedIndex = index
        }

    private fun pasteSelected() {
        entries.getOrNull(selectedIndex)?.let { entry ->
            hide()
            WriteCommandAction.runWriteCommandAction(editor.project, "Paste from Kill Ring", null, {
                EditorModificationUtil.typeInStringAtCaretHonorMultipleCarets(editor, entry)
            })
            placeCarets(entry.length)
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
    }

    /**
     * Every caret ends up after its own copy of the text, or in front of it for [asPrefix].
     *
     * The mark is pushed only for a single caret, as in ordinary paste: there is one mark ring, and several carets
     * have no single place to record. Offsets are derived from the length inserted, since a document cannot hold
     * the line separators that would make the text arrive as something other than what was handed over.
     */
    private fun placeCarets(insertedLength: Int) {
        if (editor.caretModel.caretCount == 1) {
            val caret = editor.caretModel.primaryCaret
            val end = caret.offset
            val start = end - insertedLength

            // Pushing records wherever the caret is, so it goes to the far end and then comes back.
            caret.moveToOffset(if (asPrefix) end else start)
            MarkHandler.pushPlaceInfo(editor)
            caret.moveToOffset(if (asPrefix) start else end)
        } else if (asPrefix) {
            editor.caretModel.allCarets.forEach { caret -> caret.moveToOffset(caret.offset - insertedLength) }
        }
    }

    override fun clearDelegate() {
        PasteHandler.killRingDelegate = null
    }

    /** Reports whether the chooser used the key, which decides whether anything else gets to see it. */
    private fun keyEventHandler(e: KeyEvent): Boolean {
        val ctrl = e.modifiersEx and CTRL_DOWN_MASK == CTRL_DOWN_MASK
        val owned = e.keyCode == VK_ESCAPE || e.keyCode == VK_ENTER || (ctrl && e.keyCode == VK_G)

        if (owned && e.id == KeyEvent.KEY_PRESSED) {
            when {
                e.keyCode == VK_ESCAPE || (ctrl && e.keyCode == VK_G) -> hide()
                e.keyCode == VK_ENTER -> pasteSelected()
            }
        }
        return owned
    }
}
