package com.github.strindberg.emacsj.word

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class MovementType { NEXT, PREVIOUS }

@Language("devkit-action-id")
internal const val ACTION_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.movenextword"

@Language("devkit-action-id")
internal const val ACTION_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.movepreviousword"

class WordMovementHandler(private val type: MovementType) : EditorActionHandler.ForEachCaret() {

    override fun doExecute(editor: Editor, caret: Caret, dataContext: DataContext?) {
        val offset = when (type) {
            MovementType.NEXT -> currentWordEnd(editor.text, caret.offset, editor.isCamel)
            MovementType.PREVIOUS -> currentWordStart(editor.text, caret.offset, editor.isCamel)
        }

        offset.let { caret.moveToOffset(it) }
    }
}
