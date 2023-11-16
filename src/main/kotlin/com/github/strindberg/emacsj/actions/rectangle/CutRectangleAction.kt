package com.github.strindberg.emacsj.actions.rectangle

import com.github.strindberg.emacsj.rectangle.RectangleHandler
import com.github.strindberg.emacsj.rectangle.Type
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CutRectangleAction : TextComponentEditorAction(RectangleHandler(Type.CUT))
