package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchEditHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchEditAction :
    EditorAction(ISearchEditHandler()),
    ISearchAction
