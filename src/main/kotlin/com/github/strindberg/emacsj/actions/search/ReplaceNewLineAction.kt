package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplaceNewLineHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplaceNewLineAction : ReplaceAction, EditorAction(ReplaceNewLineHandler())
