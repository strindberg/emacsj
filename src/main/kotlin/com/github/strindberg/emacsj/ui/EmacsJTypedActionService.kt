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

    init {
        TypedAction.getInstance().apply {
            originalHandler = setupRawHandler(object : WrappedTypedActionHandler(rawHandler) {
                override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                    val isearchDelegate = ISearchHandler.delegate?.takeIf { it.isActive() }
                    val zapDelegate = ZapHandler.delegate
                    val universalArgumentDelegate = UniversalArgumentHandler.delegate

                    when {
                        isearchDelegate != null -> isearchDelegate.handleChar(charTyped.toString())
                        zapDelegate != null -> zapDelegate.doZap(charTyped)
                        universalArgumentDelegate != null -> universalArgumentDelegate.handleChar(originalHandler, charTyped)
                        else -> originalHandler.execute(editor, charTyped, dataContext)
                    }
                }
            })
        }
    }

    override fun dispose() {
        TypedAction.getInstance().setupRawHandler(originalHandler)
    }

    companion object {
        val instance
            get(): EmacsJTypedActionService = ApplicationManager.getApplication().getService(EmacsJTypedActionService::class.java)
    }
}

private abstract class WrappedTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandler
