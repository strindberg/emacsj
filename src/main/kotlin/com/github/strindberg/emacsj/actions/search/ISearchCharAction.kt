package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ExpandType
import com.github.strindberg.emacsj.search.ISearchExpandHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchCharAction :
    EditorAction(ISearchExpandHandler(ExpandType.CHARACTER)),
    ISearchAction
