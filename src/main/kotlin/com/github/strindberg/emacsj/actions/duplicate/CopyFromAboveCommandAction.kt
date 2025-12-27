package com.github.strindberg.emacsj.actions.duplicate

import com.github.strindberg.emacsj.duplicate.CopyFromAboveCommandHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CopyFromAboveCommandAction : TextComponentEditorAction(CopyFromAboveCommandHandler())
