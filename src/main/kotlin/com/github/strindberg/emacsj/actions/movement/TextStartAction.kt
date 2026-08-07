package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.TextMovementHandler
import com.github.strindberg.emacsj.movement.TextMovementType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class TextStartAction : TextComponentEditorAction(TextMovementHandler(TextMovementType.START))
