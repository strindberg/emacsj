package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchExpandHandler
import com.github.strindberg.emacsj.search.Type
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchLineAction : ISearchAction, EditorAction(ISearchExpandHandler(Type.LINE))
