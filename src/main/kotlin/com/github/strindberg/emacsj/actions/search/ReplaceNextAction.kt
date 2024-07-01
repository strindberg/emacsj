package com.github.strindberg.emacsj.actions.search

import com.github.strindberg.emacsj.search.ReplacePreviousHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ReplaceNextAction :
    EditorAction(ReplacePreviousHandler(true)),
    ReplaceAction
