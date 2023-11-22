package com.github.strindberg.emacsj.actions.view

import com.github.strindberg.emacsj.view.RepositionHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class RepositionAction : EditorAction(RepositionHandler())
