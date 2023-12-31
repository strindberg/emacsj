package com.github.strindberg.emacsj.view

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class RepositionHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret

        val viewOffset = editor.scrollingModel.verticalScrollOffset
        val viewHeight = editor.scrollingModel.visibleArea.height

        val topPos = VisualPosition(viewOffset / editor.lineHeight + 1, 0)
        val middlePos = VisualPosition((viewOffset + viewHeight / 2) / editor.lineHeight, 0)
        val bottomPos = VisualPosition((viewOffset + viewHeight) / editor.lineHeight - 1, 0)

        primary.moveToVisualPosition(
            when (primary.caretModel.visualPosition) {
                middlePos -> topPos
                topPos -> bottomPos
                else -> middlePos
            }
        )
    }
}
