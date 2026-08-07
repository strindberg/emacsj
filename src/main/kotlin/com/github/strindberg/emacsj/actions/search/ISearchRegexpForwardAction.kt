package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.SearchDirection
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchRegexpForwardAction :
    EditorAction(ISearchHandler(direction = SearchDirection.FORWARD, type = SearchType.REGEXP)),
    ISearchAction
