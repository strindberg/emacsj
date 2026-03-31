package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchToggleRegexpHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchToggleRegexpAction :
    EditorAction(ISearchToggleRegexpHandler()),
    ISearchAction
