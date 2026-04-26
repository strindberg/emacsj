package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ISearchReplaceHandler
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ISearchReplaceRegexpAction : EditorAction(ISearchReplaceHandler(SearchType.REGEXP)), ISearchAction
