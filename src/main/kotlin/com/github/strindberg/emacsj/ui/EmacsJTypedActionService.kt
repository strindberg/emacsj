package com.github.strindberg.emacsj.ui

import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

@Service
internal class EmacsJTypedActionService : Disposable {

    internal val originalHandler: TypedActionHandler

    companion object {
        val instance
            get(): EmacsJTypedActionService = ApplicationManager.getApplication().getService(EmacsJTypedActionService::class.java)
    }

    init {
        TypedAction.getInstance().apply {
            originalHandler = setupRawHandler(object : WrappedTypedActionHandler(rawHandler) {
                @Suppress("ReturnCount")
                override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                    // Only while the search is running. Once its text is being edited the popup's own editor owns the keystroke.
                    ISearchHandler.delegate?.takeIf { it.isActive() }?.let { delegate ->
                        delegate.handleChar(charTyped.toString())
                        return
                    }
                    ZapHandler.delegate?.let { delegate ->
                        delegate.doZap(charTyped)
                        return
                    }
                    UniversalArgumentHandler.delegate?.let { delegate ->
                        delegate.handleChar(originalHandler, charTyped)
                        return
                    }
                    originalHandler.execute(editor, charTyped, dataContext)
                }
            })
        }
    }

    override fun dispose() {
        TypedAction.getInstance().setupRawHandler(originalHandler)
    }
}

private abstract class WrappedTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandler
