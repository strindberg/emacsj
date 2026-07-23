package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchEnterHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchEnterAction :
    EditorAction(ISearchEnterHandler()),
    ISearchAction
