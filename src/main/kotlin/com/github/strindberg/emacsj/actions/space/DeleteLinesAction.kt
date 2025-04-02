package com.github.strindberg.emacsj.actions.space

import com.github.strindberg.emacsj.space.DeleteLinesHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class DeleteLinesAction : TextComponentEditorAction(DeleteLinesHandler())
