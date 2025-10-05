package com.github.strindberg.emacsj.actions.line

import com.github.strindberg.emacsj.line.TransposeLinesHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class TransposeLinesAction : EditorAction(TransposeLinesHandler())
