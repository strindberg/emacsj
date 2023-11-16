package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplacePreviousHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplacePreviousAction : ReplaceAction, EditorAction(ReplacePreviousHandler(false))
