package com.github.strindberg.emacsj.movement

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.movement.MovementType.END
import com.github.strindberg.emacsj.movement.MovementType.START
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.EditorActionUtil
import org.intellij.lang.annotations.Language

enum class MovementType { START, END }

@Language("devkit-action-id")
internal const val ACTION_TEXT_START = "com.github.strindberg.emacsj.actions.movement.textstart"

@Language("devkit-action-id")
internal const val ACTION_TEXT_END = "com.github.strindberg.emacsj.actions.movement.textend"

class TextMovementHandler(val type: MovementType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (!editor.selectionModel.hasSelection()) {
            MarkHandler.pushPlaceInfo(editor)
        }
        when (type) {
            START -> EditorActionUtil.moveCaretToTextStart(editor, CommonDataKeys.PROJECT.getData(dataContext))
            END -> EditorActionUtil.moveCaretToTextEnd(editor, CommonDataKeys.PROJECT.getData(dataContext))
        }
    }
}
