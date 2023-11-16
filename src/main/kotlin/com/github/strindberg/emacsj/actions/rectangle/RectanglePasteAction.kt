package com.github.strindberg.emacsj.actions.rectangle

import com.github.strindberg.emacsj.rectangle.RectanglePasteHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class RectanglePasteAction : TextComponentEditorAction(RectanglePasteHandler())
