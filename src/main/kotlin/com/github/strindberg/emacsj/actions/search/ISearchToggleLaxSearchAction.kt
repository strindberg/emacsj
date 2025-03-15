package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchToggleLaxSearchHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchToggleLaxSearchAction :
    EditorAction(ISearchToggleLaxSearchHandler()),
    ISearchAction
