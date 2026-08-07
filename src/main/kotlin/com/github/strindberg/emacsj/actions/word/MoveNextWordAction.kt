package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.WordMovementHandler
import com.github.strindberg.emacsj.word.WordMovementType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class MoveNextWordAction : TextComponentEditorAction(WordMovementHandler(WordMovementType.NEXT))
