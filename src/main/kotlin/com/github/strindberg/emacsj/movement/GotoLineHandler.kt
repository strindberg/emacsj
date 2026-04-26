package com.github.strindberg.emacsj.movement

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_GOTO_LINE = "com.github.strindberg.emacsj.actions.movement.gotoline"

class GotoLineHandler : EditorActionHandler() {

    companion object {
        internal var delegate: GotoLineDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        delegate = GotoLineDelegate(editor)
    }
}
