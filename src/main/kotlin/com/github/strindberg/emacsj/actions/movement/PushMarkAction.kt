package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.MarkHandler
import com.github.strindberg.emacsj.movement.Type
import com.intellij.openapi.editor.actionSystem.EditorAction

class PushMarkAction : EditorAction(MarkHandler(Type.PUSH))
