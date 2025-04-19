package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

internal const val ACTION_ISEARCH_MARK = "com.github.strindberg.emacsj.actions.search.isearchmark"

class ISearchMarkHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.state == ISearchState.SEARCH || it.state == ISearchState.FAILED }
            ?.markSearchStopAndThenCancel()
    }
}
