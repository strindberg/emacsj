package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class ISearchToggleLaxWhitespaceHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.state in listOf(ISearchState.SEARCH, ISearchState.FAILED) }?.let { delegate ->
            delegate.swapLax()
        }
    }
}
