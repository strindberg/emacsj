package com.github.strindberg.emacsj.view

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class RecenterHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret

        val viewOffset = editor.scrollingModel.verticalScrollOffset
        val viewHeight = editor.scrollingModel.visibleArea.height
        val caretOffset = editor.visualPositionToXY(primary.visualPosition).y
        val editorEnd = editor.visualPositionToXY(VisualPosition(editor.document.lineCount, 0)).y

        val scrollTop = caretOffset - editor.lineHeight
        val scrollMiddle = caretOffset - viewHeight / 2
        val scrollBottom = caretOffset - viewHeight + 2 * editor.lineHeight

        editor.scrollingModel.scrollVertically(
            when (viewOffset) {
                scrollMiddle -> scrollTop
                scrollTop -> scrollBottom
                scrollBottom -> scrollMiddle
                else -> {
                    if (caretOffset < viewHeight / 2) {
                        scrollTop
                    } else if (editorEnd - caretOffset < viewHeight / 2) {
                        scrollBottom
                    } else {
                        scrollMiddle
                    }
                }
            }
        )
    }
}
