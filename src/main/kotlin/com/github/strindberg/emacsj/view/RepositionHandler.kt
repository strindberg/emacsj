package com.github.strindberg.emacsj.view

import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.view.Position.BOTTOM
import com.github.strindberg.emacsj.view.Position.MIDDLE
import com.github.strindberg.emacsj.view.Position.TOP
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_REPOSITION = "com.github.strindberg.emacsj.actions.view.reposition"

class RepositionHandler : EditorActionHandler() {

    private var lastPosition = MIDDLE

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        editor.caretModel.removeSecondaryCarets()

        val viewOffset = editor.scrollingModel.verticalScrollOffset
        val viewHeight = editor.scrollingModel.visibleArea.height

        val topPos = VisualPosition(viewOffset / editor.lineHeight + 1, 0)
        val middlePos = VisualPosition((viewOffset + viewHeight / 2) / editor.lineHeight, 0)
        val bottomPos = VisualPosition((viewOffset + viewHeight) / editor.lineHeight - 1, 0)

        if (EmacsJCommandListener.lastCommandName == EmacsJBundle.actionText(ACTION_REPOSITION) && lastPosition == MIDDLE) {
            lastPosition = TOP
            primary.moveToVisualPosition(topPos)
        } else if (EmacsJCommandListener.lastCommandName == EmacsJBundle.actionText(ACTION_REPOSITION) && lastPosition == TOP) {
            lastPosition = BOTTOM
            primary.moveToVisualPosition(bottomPos)
        } else {
            lastPosition = MIDDLE
            primary.moveToVisualPosition(middlePos)
        }
    }
}
