package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchSwapHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchSwapAction :
    EditorAction(ISearchSwapHandler()),
    ISearchAction
