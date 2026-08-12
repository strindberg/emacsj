package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.EmacsJService
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_PASTE_HISTORY = "com.github.strindberg.emacsj.actions.search.isearchpastehistory"

private val isearchPasteActionIds = setOf(ACTION_ISEARCH_PASTE, ACTION_ISEARCH_PASTE_HISTORY)

internal class ISearchPasteHistoryHandler : EditorActionHandler() {

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
        ISearchHandler.delegate?.isActive() == true

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (EmacsJService.instance.lastActionId() in isearchPasteActionIds) {
            ISearchHandler.delegate?.pasteNextInHistory()
        }
    }
}
