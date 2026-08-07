package com.github.strindberg.emacsj.actions.mark

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.MarkType
import com.intellij.openapi.editor.actionSystem.EditorAction

class PushMarkAction : EditorAction(MarkHandler(MarkType.PUSH))
