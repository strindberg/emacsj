package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_PREVIOUS = "com.github.strindberg.emacsj.actions.search.isearchprevious"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_NEXT = "com.github.strindberg.emacsj.actions.search.isearchnext"

class ISearchPreviousHandler(private val isForward: Boolean) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.let { delegate ->
            delegate.editPrevious(
                if (isForward) ISearchHandler.getNext(delegate.searchType) else ISearchHandler.getPrevious(delegate.searchType)
            )
        }
    }
}
