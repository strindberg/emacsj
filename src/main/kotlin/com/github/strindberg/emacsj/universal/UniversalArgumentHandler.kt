package com.github.strindberg.emacsj.universal

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class UniversalArgumentHandler(val numeric: Int?) : EditorActionHandler() {

    companion object {
        internal var delegate: UniversalArgumentDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (numeric == null) {
                current.multiply()
            } else {
                current.addDigit(numeric)
            }
        } else {
            delegate = UniversalArgumentDelegate(editor, dataContext, numeric)
        }
    }
}
