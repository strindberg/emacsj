package com.github.strindberg.emacsj.actions.movement

import com.github.strindberg.emacsj.movement.MovementType
import com.github.strindberg.emacsj.movement.TextMovementHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class TextStartAction : TextComponentEditorAction(TextMovementHandler(MovementType.START))
