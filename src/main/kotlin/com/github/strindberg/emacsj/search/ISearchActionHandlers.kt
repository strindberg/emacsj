package com.github.strindberg.emacsj.search

import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

internal class ISearchActionHandler(
    val action: String,
    val originalHandler: EditorActionHandler,
    val doExecute: ISearchDelegate.(caret: Caret?, dataContext: DataContext) -> Unit,
) : EditorActionHandler() {

    public override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
        return ISearchHandler.delegate != null || originalHandler.isEnabled(editor, caret, dataContext)
    }

    public override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val delegate = ISearchHandler.delegate
        if (delegate == null) {
            originalHandler.execute(editor, caret, dataContext)
        } else {
            delegate.doExecute(caret, dataContext)
        }
    }
}

internal abstract class ISearchTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandlerBase(originalHandler)
