package com.github.strindberg.emacsj.actions.space

import com.github.strindberg.emacsj.space.DeleteSpaceHandler
import com.github.strindberg.emacsj.space.SpaceType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class OneSpaceAction : TextComponentEditorAction(DeleteSpaceHandler(SpaceType.ONE_SPACE))
