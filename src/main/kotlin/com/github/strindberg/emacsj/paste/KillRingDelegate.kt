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
            val origin = editor.caretModel.offset
            hide()
            WriteCommandAction.runWriteCommandAction(editor.project, "Paste from Kill Ring", null, {
                EditorModificationUtil.insertStringAtCaret(editor, entry)
            })
            val end = editor.caretModel.offset

            // The mark is left at whichever end of the pasted text the caret does not occupy.
            editor.caretModel.moveToOffset(if (asPrefix) end else origin)
            MarkHandler.pushPlaceInfo(editor)
            editor.caretModel.moveToOffset(if (asPrefix) origin else end)

            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
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
