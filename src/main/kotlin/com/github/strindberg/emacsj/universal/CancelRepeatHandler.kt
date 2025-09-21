package com.github.strindberg.emacsj.universal

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_CANCEL_REPEAT = "com.github.strindberg.emacsj.actions.universal.cancelrepeat"

class CancelRepeatHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        UniversalArgumentHandler.canceled = true
    }
}
