package com.github.strindberg.emacsj.actions.zap

import com.github.strindberg.emacsj.zap.ZapHandler
import com.github.strindberg.emacsj.zap.ZapType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class ZapToCharAction : TextComponentEditorAction(ZapHandler(ZapType.FORWARD_TO))
