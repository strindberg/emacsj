package com.github.strindberg.emacsj.view

import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.view.Position.BOTTOM
import com.github.strindberg.emacsj.view.Position.MIDDLE
import com.github.strindberg.emacsj.view.Position.TOP
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class Position { TOP, MIDDLE, BOTTOM }

@Language("devkit-action-id")
internal const val ACTION_RECENTER = "com.github.strindberg.emacsj.actions.view.recenter"

private const val COMMAND_RECENTER = "Recenter Caret"

class RecenterHandler : EditorActionHandler() {

    private var lastPosition = MIDDLE

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret

        val viewHeight = editor.scrollingModel.visibleArea.height
        val caretOffset = editor.visualPositionToXY(primary.visualPosition).y

        val scrollTop = caretOffset - editor.lineHeight
        val scrollMiddle = caretOffset - viewHeight / 2
        val scrollBottom = caretOffset - viewHeight + 2 * editor.lineHeight

        if (EmacsJCommandListener.lastCommandName == COMMAND_RECENTER && lastPosition == MIDDLE) {
            lastPosition = TOP
            editor.scrollingModel.scrollVertically(scrollTop)
        } else if (EmacsJCommandListener.lastCommandName == COMMAND_RECENTER && lastPosition == TOP) {
            lastPosition = BOTTOM
            editor.scrollingModel.scrollVertically(scrollBottom)
        } else {
            lastPosition = MIDDLE
            editor.scrollingModel.scrollVertically(scrollMiddle)
        }
    }
}
