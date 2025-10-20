package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchToggleCaseHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchToggleCaseAction :
    EditorAction(ISearchToggleCaseHandler()),
    ISearchAction
