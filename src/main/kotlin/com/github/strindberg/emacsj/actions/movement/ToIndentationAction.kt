package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.ToIndentationHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class ToIndentationAction : TextComponentEditorAction(ToIndentationHandler())
