package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchPreviousHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchNextAction : ISearchAction, EditorAction(ISearchPreviousHandler(true))
