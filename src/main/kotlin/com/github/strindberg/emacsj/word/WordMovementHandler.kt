package com.github.strindberg.emacsj.word

import java.lang.invoke.MethodHandles
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

@Suppress("unused")
private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

enum class MovementType { NEXT, PREVIOUS }

class WordMovementHandler(private val type: MovementType) : EditorActionHandler.ForEachCaret() {

    override fun doExecute(editor: Editor, caret: Caret, dataContext: DataContext?) {
        val offset = when (type) {
            MovementType.NEXT -> currentWordEnd(editor, caret.offset)
            MovementType.PREVIOUS -> currentWordStart(editor, caret.offset)
        }

        offset?.let { caret.moveToOffset(it) }
    }
}
