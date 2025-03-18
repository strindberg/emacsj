package com.github.strindberg.emacsj.movement

import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.EditorActionUtil

enum class MovementType { START, END }

class TextMovementHandler(val type: MovementType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        MarkHandler.pushPlaceInfo(editor)
        when (type) {
            MovementType.START -> EditorActionUtil.moveCaretToTextStart(editor, CommonDataKeys.PROJECT.getData(dataContext))
            MovementType.END -> EditorActionUtil.moveCaretToTextEnd(editor, CommonDataKeys.PROJECT.getData(dataContext))
        }
    }
}
