package com.github.strindberg.emacsj.space

import java.lang.invoke.MethodHandles
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

@Suppress("unused")
private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

class OneSpaceHandler : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val start = previousNonWhiteSpace(editor.text, caret.offset)
        val end = nextNonWhiteSpace(editor.text, caret.offset)

        editor.document.replaceString(start, end, " ")
        if (start >= caret.offset) {
            caret.moveToOffset(caret.offset + 1)
        }
    }
}
