package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.SearchDirection
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchTextBackwardAction :
    EditorAction(ISearchHandler(direction = SearchDirection.BACKWARD, type = SearchType.TEXT)),
    ISearchAction
