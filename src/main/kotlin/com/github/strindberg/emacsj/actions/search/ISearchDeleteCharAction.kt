package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchDeleteCharHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchDeleteCharAction :
    EditorAction(ISearchDeleteCharHandler()),
    ISearchAction
