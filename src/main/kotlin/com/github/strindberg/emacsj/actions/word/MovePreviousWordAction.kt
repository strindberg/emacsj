package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.MovementType
import com.github.strindberg.emacsj.word.WordMovementHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class MovePreviousWordAction : TextComponentEditorAction(WordMovementHandler(MovementType.PREVIOUS))
