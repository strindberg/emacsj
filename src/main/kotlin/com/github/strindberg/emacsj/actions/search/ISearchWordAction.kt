package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ExpandType
import com.github.strindberg.emacsj.search.ISearchExpandHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchWordAction :
    EditorAction(ISearchExpandHandler(ExpandType.WORD)),
    ISearchAction
