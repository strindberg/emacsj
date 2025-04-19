package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

internal const val ACTION_ISEARCH_SWAP = "com.github.strindberg.emacsj.actions.search.isearchswap"

class ISearchSwapHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.state == ISearchState.SEARCH || it.state == ISearchState.FAILED }
            ?.swapSearchStopAndThenCancel()
    }
}
