package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.FirstLastType
import com.github.strindberg.emacsj.search.ISearchFirstLastHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchLastMatchAction :
    EditorAction(ISearchFirstLastHandler(FirstLastType.LAST)),
    ISearchAction
