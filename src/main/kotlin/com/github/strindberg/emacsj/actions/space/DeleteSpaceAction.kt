package com.github.strindberg.emacsj.actions.space

import com.github.strindberg.emacsj.space.DeleteSpaceHandler
import com.github.strindberg.emacsj.space.SpaceType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class DeleteSpaceAction : TextComponentEditorAction(DeleteSpaceHandler(SpaceType.DELETE))
