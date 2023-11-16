package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplacePreviousHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplaceNexAction : ReplaceAction, EditorAction(ReplacePreviousHandler(true))
