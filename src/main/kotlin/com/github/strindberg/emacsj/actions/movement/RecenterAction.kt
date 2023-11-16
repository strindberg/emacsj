package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.RecenterHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class RecenterAction : EditorAction(RecenterHandler())
