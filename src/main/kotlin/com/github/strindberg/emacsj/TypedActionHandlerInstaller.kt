package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

@Service
internal class TypedActionHandlerInstaller : Disposable {

    private val handler: RestorableTypedActionHandler

    init {
        val typedAction = TypedAction.getInstance()
        handler = object : RestorableTypedActionHandler(typedAction.rawHandler) {
            @Suppress("ReturnCount")
            override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                ISearchHandler.delegate?.let { delegate ->
                    delegate.handleChar(charTyped)
                    return
                }
                ZapHandler.delegate?.let { delegate ->
                    delegate.doZap(charTyped)
                    return
                }
                UniversalArgumentHandler.delegate?.let { delegate ->
                    myOriginalHandler?.let { originalHandler ->
                        delegate.handleChar(originalHandler, charTyped)
                    }
                    return
                }
                myOriginalHandler?.execute(editor, charTyped, dataContext)
            }
        }
        typedAction.setupRawHandler(handler)
    }

    override fun dispose() {
        TypedAction.getInstance().setupRawHandler(handler.originalHandler)
    }
}

internal abstract class RestorableTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandlerBase(originalHandler)
