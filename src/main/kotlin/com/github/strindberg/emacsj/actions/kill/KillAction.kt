package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.KillHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class KillAction : TextComponentEditorAction(KillHandler())
