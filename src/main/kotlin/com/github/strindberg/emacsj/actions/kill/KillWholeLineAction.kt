package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.KillLineHandler
import com.github.strindberg.emacsj.kill.KillType
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class KillWholeLineAction : TextComponentEditorAction(KillLineHandler(KillType.WHOLE_LINE))
