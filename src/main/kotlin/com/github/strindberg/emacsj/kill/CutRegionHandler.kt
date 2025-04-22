package com.github.strindberg.emacsj.kill

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.util.DocumentUtil

internal const val ACTION_CUT = "com.github.strindberg.emacsj.actions.kill.cut"

class CutRegionHandler : EditorWriteActionHandler() {

    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        if (editor.selectionModel.hasSelection()) {
            KillUtil.cut(
                editor = editor,
                textStartOffset = editor.selectionModel.selectionStart,
                textEndOffset = editor.selectionModel.selectionEnd,
                prepend = primary.offset == editor.selectionModel.selectionStart,
            )
        } else {
            KillUtil.cut(
                editor = editor,
                textStartOffset = DocumentUtil.getLineStartOffset(primary.offset, editor.document),
                textEndOffset = minOf(editor.document.textLength, DocumentUtil.getLineEndOffset(primary.offset, editor.document) + 1),
            )
        }
    }
}
