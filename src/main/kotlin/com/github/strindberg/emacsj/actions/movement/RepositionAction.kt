package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.RepositionHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class RepositionAction : EditorAction(RepositionHandler())
