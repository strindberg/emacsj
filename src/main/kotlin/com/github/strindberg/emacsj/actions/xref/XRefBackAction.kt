package com.github.strindberg.emacsj.actions.xref

import com.github.strindberg.emacsj.xref.XRefHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class XRefBackAction : TextComponentEditorAction(XRefHandler())
