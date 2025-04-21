package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.AppendNextKillHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class AppendNextKillAction : TextComponentEditorAction(AppendNextKillHandler())
