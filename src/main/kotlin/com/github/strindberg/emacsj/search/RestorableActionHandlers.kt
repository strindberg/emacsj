package com.github.strindberg.emacsj.search

import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

internal class RestorableActionHandler<T>(
    val actionId: String,
    val originalHandler: EditorActionHandler,
    val getDelegate: () -> T?,
    val doExecute: T.(caret: Caret?, dataContext: DataContext) -> Unit,
) : EditorActionHandler() {

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
        getDelegate() != null || originalHandler.isEnabled(editor, caret, dataContext)

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val delegate = getDelegate()
        if (delegate == null) {
            originalHandler.execute(editor, caret, dataContext)
        } else {
            delegate.doExecute(caret, dataContext)
        }
    }
}

internal abstract class RestorableTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandlerBase(originalHandler)
