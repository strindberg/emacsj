package com.github.strindberg.emacsj.actions.xref

import com.github.strindberg.emacsj.xref.XRefHandler
import com.github.strindberg.emacsj.xref.XRefType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class XRefForwardAction : TextComponentEditorAction(XRefHandler(XRefType.FORWARD))
