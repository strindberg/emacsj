package com.github.strindberg.emacsj.word

import java.lang.invoke.MethodHandles
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

enum class MovementType { NEXT, PREVIOUS }

class WordMovementHandler(private val type: MovementType) : EditorActionHandler.ForEachCaret() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    override fun doExecute(editor: Editor, caret: Caret, dataContext: DataContext?) {
        val offset = when (type) {
            MovementType.NEXT -> currentWordEnd(editor.text, caret.offset, editor.isCamel)
            MovementType.PREVIOUS -> currentWordStart(editor.text, caret.offset, editor.isCamel)
        }

        offset.let { caret.moveToOffset(it) }
    }
}
