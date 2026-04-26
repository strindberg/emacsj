package com.github.strindberg.emacsj.search

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_REPLACE_TEXT = "com.github.strindberg.emacsj.actions.search.isearchreplacetext"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_REPLACE_REGEXP = "com.github.strindberg.emacsj.actions.search.isearchreplaceregexp"

class ISearchReplaceHandler(private val type: SearchType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.isActive() }?.let { searchDelegate ->
            val replaceDelegate = ReplaceDelegate(editor = editor, type = type)
            replaceDelegate.text = searchDelegate.text
            replaceDelegate.setReplaceState()

            ReplaceHandler.delegate = replaceDelegate
            editor.caretModel.primaryCaret.run { moveToOffset(search.match.start) }

            searchDelegate.hide()
            replaceDelegate.show()
        }
    }
}
