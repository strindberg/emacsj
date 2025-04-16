package com.github.strindberg.emacsj.universal

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class UniversalArgumentHandler : EditorActionHandler() {

    companion object {
        internal var delegate: UniversalArgumentDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            current.multiply()
        } else {
            delegate = UniversalArgumentDelegate(editor, dataContext)
        }
    }
}
