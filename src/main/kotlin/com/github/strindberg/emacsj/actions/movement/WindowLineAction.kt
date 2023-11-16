package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.WindowLineHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class WindowLineAction : EditorAction(WindowLineHandler())
