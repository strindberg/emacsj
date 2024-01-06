package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class ReplacePreviousHandler(private val forward: Boolean) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ReplaceHandler.delegate?.let { delegate ->
            val previous = if (forward) ReplaceHandler.getNext(delegate.type) else ReplaceHandler.getPrevious(delegate.type)
            if (delegate.state == ReplaceState.GET_SEARCH_ARG) {
                delegate.text = previous.search
            } else if (delegate.state == ReplaceState.GET_REPLACE_ARG) {
                delegate.text = previous.replace
            }
        }
    }
}
