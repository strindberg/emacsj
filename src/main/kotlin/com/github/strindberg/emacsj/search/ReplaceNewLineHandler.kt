package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_REPLACE_NEWLINE = "com.github.strindberg.emacsj.actions.search.replacenewline"

class ReplaceNewLineHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ReplaceHandler.delegate?.let { delegate ->
            if (delegate.state in listOf(ReplaceState.GET_SEARCH_ARG, ReplaceState.GET_REPLACE_ARG)) {
                delegate.text += "\n"
            }
        }
    }
}
