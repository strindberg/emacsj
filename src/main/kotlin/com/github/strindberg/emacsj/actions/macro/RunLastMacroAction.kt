package com.github.strindberg.emacsj.actions.macro

import com.github.strindberg.emacsj.macro.RunLastMacroHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class RunLastMacroAction : EditorAction(RunLastMacroHandler())
