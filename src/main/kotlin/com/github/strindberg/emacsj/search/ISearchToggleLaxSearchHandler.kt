package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

internal const val ACTION_TOGGLE_LAX_SEARCH = "com.github.strindberg.emacsj.actions.search.togglelaxsearch"

class ISearchToggleLaxSearchHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.swapLax()
        ISearchHandler.delegate?.renewState()
    }
}
