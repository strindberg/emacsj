package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplaceRegexpAction : EditorAction(ReplaceHandler(type = SearchType.REGEXP))
