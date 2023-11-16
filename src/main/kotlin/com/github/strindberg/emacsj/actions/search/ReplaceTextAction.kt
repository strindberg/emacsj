package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.search.SearchType
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplaceTextAction : EditorAction(ReplaceHandler(type = SearchType.TEXT))
