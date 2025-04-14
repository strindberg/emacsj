package com.github.strindberg.emacsj.kill

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class CopyRegionHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        if (editor.selectionModel.hasSelection()) {
            KillUtil.copy(
                editor = editor,
                textStartOffset = editor.selectionModel.selectionStart,
                textEndOffset = editor.selectionModel.selectionEnd,
                prepend = primary.offset == editor.selectionModel.selectionStart,
            )
        }
    }
}
