package com.github.strindberg.emacsj.word

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class WordMovementType { NEXT, PREVIOUS }

@Language("devkit-action-id")
internal const val ACTION_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.movenextword"

@Language("devkit-action-id")
internal const val ACTION_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.movepreviousword"

internal class WordMovementHandler(private val type: WordMovementType) : EditorActionHandler.ForEachCaret() {

    override fun doExecute(editor: Editor, caret: Caret, dataContext: DataContext?) {
        val offset = when (type) {
            WordMovementType.NEXT -> currentWordEnd(editor.text, caret.offset, editor.isCamel)
            WordMovementType.PREVIOUS -> currentWordStart(editor.text, caret.offset, editor.isCamel)
        }

        caret.moveToOffset(offset)
    }
}
