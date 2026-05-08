package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.Direction
import com.github.strindberg.emacsj.word.TransposeWordHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class TransposeWordsReverseAction : TextComponentEditorAction(TransposeWordHandler(Direction.BACKWARD))
