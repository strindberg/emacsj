package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx

class XRefHandler : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        (editor as? EditorEx)?.let { ex ->
            EmacsJCommandListener.popPlace()?.let { place ->
                MarkHandler.gotoPlaceInfo(editor, place)
            }
        }
    }
}
