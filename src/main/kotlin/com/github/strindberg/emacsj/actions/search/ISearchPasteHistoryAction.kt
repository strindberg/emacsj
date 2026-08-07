package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchPasteHistoryHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchPasteHistoryAction :
    EditorAction(ISearchPasteHistoryHandler()),
    ISearchAction
