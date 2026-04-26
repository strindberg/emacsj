package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.GotoLineHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class GotoLineAction : TextComponentEditorAction(GotoLineHandler())
