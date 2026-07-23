package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchBackspaceHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchBackspaceAction :
    EditorAction(ISearchBackspaceHandler()),
    ISearchAction
