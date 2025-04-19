package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.search.startStickySelection
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx

internal const val ACTION_EXCHANGE_MARK = "com.github.strindberg.emacsj.actions.mark.exchangepointandmark"

class ExchangePointAndMarkHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret

        (editor as? EditorEx)?.let { ex ->
            if (primary.hasSelection()) {
                val selectionStart = primary.selectionStart
                val selectionEnd = primary.selectionEnd

                MarkHandler.pushPlaceInfo(editor)

                ex.startStickySelection()
                primary.moveToOffset(if (primary.offset == selectionEnd) selectionStart else selectionEnd)
            } else {
                MarkHandler.peek(editor)?.caretPosition?.let { oldMark ->
                    ex.startStickySelection()
                    primary.moveToOffset(oldMark)
                }
            }
            ex.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
    }
}
