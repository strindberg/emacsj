package com.github.strindberg.emacsj.kill

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_APPEND_NEXT_KILL = "com.github.strindberg.emacsj.actions.kill.append"

class AppendNextKillHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        HintManager.getInstance().showInformationHint(editor, "If the next command is a kill, it will append")
    }
}
