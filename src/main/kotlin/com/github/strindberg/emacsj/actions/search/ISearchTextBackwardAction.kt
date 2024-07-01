package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.Direction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchTextBackwardAction :
    EditorAction(ISearchHandler(direction = Direction.BACKWARD, type = SearchType.TEXT)),
    ISearchAction
