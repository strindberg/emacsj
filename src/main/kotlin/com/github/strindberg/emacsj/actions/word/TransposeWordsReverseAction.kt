package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.Direction
import com.github.strindberg.emacsj.word.WordTransposeHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class TransposeWordsReverseAction : TextComponentEditorAction(WordTransposeHandler(Direction.BACKWARD))
