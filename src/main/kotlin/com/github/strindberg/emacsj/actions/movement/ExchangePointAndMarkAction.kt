package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.ExchangePointAndMarkHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class ExchangePointAndMarkAction : EditorAction(ExchangePointAndMarkHandler())
