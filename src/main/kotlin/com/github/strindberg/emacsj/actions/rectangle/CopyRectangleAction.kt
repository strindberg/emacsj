package com.github.strindberg.emacsj.actions.rectangle

import com.github.strindberg.emacsj.rectangle.RectangleHandler
import com.github.strindberg.emacsj.rectangle.RectangleType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CopyRectangleAction : TextComponentEditorAction(RectangleHandler(RectangleType.COPY))
