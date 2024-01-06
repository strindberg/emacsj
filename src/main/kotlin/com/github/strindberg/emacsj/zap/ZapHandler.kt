package com.github.strindberg.emacsj.zap

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

enum class ZapType { FORWARD_TO, FORWARD_UP_TO, BACKWARD_TO, BACKWARD_UP_TO }

class ZapHandler(private val type: ZapType) : EditorActionHandler() {

    companion object {
        internal var delegate: ZapDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        delegate = ZapDelegate(editor, type)
    }
}
