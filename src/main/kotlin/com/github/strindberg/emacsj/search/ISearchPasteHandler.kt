package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.search.ISearchState.EDIT
import com.github.strindberg.emacsj.search.ISearchState.FAILED
import com.github.strindberg.emacsj.search.ISearchState.SEARCH
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ex.ClipboardUtil
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_PASTE = "com.github.strindberg.emacsj.actions.search.isearchpaste"

class ISearchPasteHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.let { delegate ->
            when (delegate.state) {
                EDIT -> delegate.text += ClipboardUtil.getTextInClipboard()
                SEARCH, FAILED -> delegate.searchAllCarets(
                    searchDirection = delegate.direction,
                    newText = ClipboardUtil.getTextInClipboard().orEmpty()
                )
            }
        }
    }
}
