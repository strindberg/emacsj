package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.Direction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchTextForwardAction : ISearchAction, EditorAction(ISearchHandler(direction = Direction.FORWARD, type = SearchType.TEXT))
