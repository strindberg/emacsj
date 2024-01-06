package com.github.strindberg.emacsj.actions.zap

import com.github.strindberg.emacsj.zap.ZapHandler
import com.github.strindberg.emacsj.zap.ZapType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class ZapUpToCharAction : TextComponentEditorAction(ZapHandler(ZapType.FORWARD_UP_TO))
