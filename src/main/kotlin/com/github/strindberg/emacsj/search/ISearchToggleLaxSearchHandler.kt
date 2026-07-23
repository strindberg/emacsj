package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_TOGGLE_LAX_SEARCH = "com.github.strindberg.emacsj.actions.search.isearchtogglelaxsearch"

class ISearchToggleLaxSearchHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.toggleLax()
        ISearchHandler.delegate?.renewLaxState()
    }
}
