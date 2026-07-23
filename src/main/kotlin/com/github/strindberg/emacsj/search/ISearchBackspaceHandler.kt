package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.search.ISearchState.EDIT
import com.github.strindberg.emacsj.search.ISearchState.FAILED
import com.github.strindberg.emacsj.search.ISearchState.SEARCH
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_BACKSPACE = "com.github.strindberg.emacsj.actions.search.isearchbackspace"

class ISearchBackspaceHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.let { delegate ->
            when (delegate.state) {
                EDIT -> delegate.text = delegate.text.dropLast(1)
                SEARCH, FAILED -> delegate.popBreadcrumb()
            }
        }
    }
}
