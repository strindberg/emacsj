package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class FirstLastType { FIRST, LAST }

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_FIRST = "com.github.strindberg.emacsj.actions.search.isearchfirstmatch"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_LAST = "com.github.strindberg.emacsj.actions.search.isearchlastmatch"

class ISearchFirstLastHandler(private val type: FirstLastType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.let { delegate ->
            if (delegate.state in listOf(ISearchState.SEARCH, ISearchState.FAILED)) {
                when (type) {
                    FirstLastType.FIRST -> delegate.findFirst()
                    FirstLastType.LAST -> delegate.findLast()
                }
            }
        }
    }
}
