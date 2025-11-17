package com.github.strindberg.emacsj.view

import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJService
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

private val recenterCommandName = EmacsJBundle.actionText(ACTION_RECENTER)

class RecenterHandler : EditorActionHandler() {

    private var lastPosition = MIDDLE

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret

        val viewHeight = editor.scrollingModel.visibleArea.height
        val caretOffset = editor.visualPositionToXY(primary.visualPosition).y

        val scrollTop = caretOffset - editor.lineHeight
        val scrollMiddle = caretOffset - viewHeight / 2
        val scrollBottom = caretOffset - viewHeight + 2 * editor.lineHeight

        when (EmacsJService.instance.lastCommandName()) {
            recenterCommandName if lastPosition == MIDDLE -> {
                lastPosition = TOP
                editor.scrollingModel.scrollVertically(scrollTop)
            }
            recenterCommandName if lastPosition == TOP -> {
                lastPosition = BOTTOM
                editor.scrollingModel.scrollVertically(scrollBottom)
            }
            else -> {
                lastPosition = MIDDLE
                editor.scrollingModel.scrollVertically(scrollMiddle)
            }
        }
    }
}
