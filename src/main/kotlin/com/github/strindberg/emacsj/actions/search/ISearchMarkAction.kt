package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchMarkHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchMarkAction :
    EditorAction(ISearchMarkHandler()),
    ISearchAction
