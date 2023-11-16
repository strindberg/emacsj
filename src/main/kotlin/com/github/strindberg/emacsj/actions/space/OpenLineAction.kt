package com.github.strindberg.emacsj.actions.space

import com.github.strindberg.emacsj.space.OpenLineHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class OpenLineAction : TextComponentEditorAction(OpenLineHandler())
