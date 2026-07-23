package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchPasteHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchPasteAction :
    EditorAction(ISearchPasteHandler()),
    ISearchAction
