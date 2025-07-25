package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_MARK = "com.github.strindberg.emacsj.actions.search.isearchmark"

class ISearchMarkHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.isActive() }?.markSearchStopAndThenCancel()
    }
}
