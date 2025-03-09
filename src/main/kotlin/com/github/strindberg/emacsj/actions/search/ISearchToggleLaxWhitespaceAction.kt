package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchToggleLaxWhitespaceHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchToggleLaxWhitespaceAction :
    EditorAction(ISearchToggleLaxWhitespaceHandler()),
    ISearchAction
