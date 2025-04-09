package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.EditHandler
import com.github.strindberg.emacsj.kill.Type
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CopyAction : TextComponentEditorAction(EditHandler(Type.COPY))
