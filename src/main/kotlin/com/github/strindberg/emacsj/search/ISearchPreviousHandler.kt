package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class ISearchPreviousHandler(private val forward: Boolean) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.let { delegate ->
            delegate.state = ISearchState.CHOOSE_PREVIOUS
            if (forward) {
                delegate.text = ISearchHandler.getNext(delegate.type)
            } else {
                delegate.text = ISearchHandler.getPrevious(delegate.type)
            }
        }
    }
}
