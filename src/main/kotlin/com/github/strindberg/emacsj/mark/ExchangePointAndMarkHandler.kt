package com.github.strindberg.emacsj.mark

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx

class ExchangePointAndMarkHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        val ex = editor as EditorEx

        if (primary.hasSelection()) {
            val selectionStart = primary.selectionStart
            val selectionEnd = primary.selectionEnd

            MarkHandler.pushPlaceInfo(editor)

            ex.isStickySelection = false
            ex.isStickySelection = true // set new start of selection
            primary.moveToOffset(if (primary.offset == selectionEnd) selectionStart else selectionEnd)
        } else {
            MarkHandler.peek(editor)?.placeInfo?.caretPosition?.startOffset?.let { oldMark ->
                ex.isStickySelection = false
                ex.isStickySelection = true
                primary.moveToOffset(oldMark)
            }
        }
    }
}
