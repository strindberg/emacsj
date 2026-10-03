package com.github.strindberg.emacsj.actions.macro

import com.github.strindberg.emacsj.macro.RunLastMacroHandler
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.editor.actionSystem.EditorAction

class RunLastMacroAction : EditorAction(RunLastMacroHandler()) {

    // Matches the update thread of the platform action the handler delegates to.
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}
