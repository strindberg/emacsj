package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.TransposeWordHandler
import com.github.strindberg.emacsj.word.WordDirection
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class TransposeWordsAction : TextComponentEditorAction(TransposeWordHandler(WordDirection.FORWARD))
