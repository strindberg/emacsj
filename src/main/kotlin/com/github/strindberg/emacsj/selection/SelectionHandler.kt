package com.github.strindberg.emacsj.selection

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.ex.EditorEx
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_SELECT_NEXT_OCCURRENCE = "com.github.strindberg.emacsj.actions.selection.selectnextoccurrence"

@Language("devkit-action-id")
internal const val ACTION_SELECT_ALL_OCCURRENCES = "com.github.strindberg.emacsj.actions.selection.selectalloccurrences"

enum class SelectionType(internal val platformActionId: String) {
    NEXT(IdeActions.ACTION_SELECT_NEXT_OCCURENCE),
    ALL(IdeActions.ACTION_SELECT_ALL_OCCURRENCES),
}

/**
 * Wraps the platform's occurrence actions, which leave sticky selection switched on. Every caret move then re-extends the
 * selection from the sticky start, so the occurrences the platform just selected are immediately overwritten. See
 * https://youtrack.jetbrains.com/issue/IJPL-207535.
 *
 * Sticky selection is therefore switched off before delegating. Note that turning it off clears the selection of the current
 * caret - see `EditorImpl.isStickySelectionChanged` - so the selection is restored afterward: the platform actions search
 * for the selected text and would otherwise fall back to selecting the word under the caret.
 */
internal class SelectionHandler(private val type: SelectionType) : EditorActionHandler() {

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
        delegate().isEnabled(editor, caret, dataContext)

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        (editor as? EditorEx)?.dropStickySelection()
        delegate().execute(editor, caret, dataContext)
    }

    private fun delegate(): EditorActionHandler = EditorActionManager.getInstance().getActionHandler(type.platformActionId)
}

private fun EditorEx.dropStickySelection() {
    if (isStickySelection) {
        val hasSelection = selectionModel.hasSelection()
        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        isStickySelection = false

        if (hasSelection) {
            selectionModel.setSelection(selectionStart, selectionEnd)
        }
    }
}
