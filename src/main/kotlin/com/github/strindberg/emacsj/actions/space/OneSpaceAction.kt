package com.github.strindberg.emacsj.actions.space

import com.github.strindberg.emacsj.space.DeleteSpaceHandler
import com.github.strindberg.emacsj.space.Type
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class OneSpaceAction : TextComponentEditorAction(DeleteSpaceHandler(Type.ONE_SPACE))
