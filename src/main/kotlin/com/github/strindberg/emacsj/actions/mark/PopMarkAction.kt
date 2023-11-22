package com.github.strindberg.emacsj.actions.mark

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.Type
import com.intellij.openapi.editor.actionSystem.EditorAction

class PopMarkAction : EditorAction(MarkHandler(Type.POP))
