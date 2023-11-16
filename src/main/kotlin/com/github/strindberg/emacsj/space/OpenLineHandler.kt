package com.github.strindberg.emacsj.space

import java.lang.invoke.MethodHandles
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

@Suppress("unused")
private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

class OpenLineHandler : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        editor.document.insertString(caret.offset, "\n")
    }
}
