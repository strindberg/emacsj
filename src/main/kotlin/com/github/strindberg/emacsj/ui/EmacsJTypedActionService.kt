package com.github.strindberg.emacsj.ui

import java.awt.AWTEvent
import java.awt.Component
import java.awt.event.InputMethodEvent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.util.ui.UIUtil

@Service
internal class EmacsJTypedActionService : Disposable {

    internal val originalHandler: TypedActionHandler

    companion object {
        val instance
            get(): EmacsJTypedActionService = ApplicationManager.getApplication().getService(EmacsJTypedActionService::class.java)
    }

    init {
        TypedAction.getInstance().apply {
            originalHandler = rawHandler
            setupRawHandler(object : TypedActionHandlerBase(rawHandler) {
                @Suppress("ReturnCount")
                override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                    ISearchHandler.delegate?.let { delegate ->
                        delegate.handleChar(charTyped.toString())
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
            })
        }
    }

    override fun dispose() {
        TypedAction.getInstance().setupRawHandler(originalHandler)
    }
}

interface UIDelegate : Disposable {
    val editor: Editor
}

@OptIn(ExperimentalContracts::class)
internal fun UIDelegate?.isActive(e: AWTEvent): Boolean {
    contract {
        returns(true) implies (this@isActive != null && e is InputMethodEvent)
    }
    return this != null && e is InputMethodEvent && UIUtil.isDescendingFrom(e.source as? Component, editor.contentComponent)
}

internal fun InputMethodEvent.constructInput(): String? =
    text?.let { iter ->
        buildString {
            var c = iter.first()
            repeat(committedCharacterCount) {
                append(c)
                c = iter.next()
            }
        }.takeIf { it.isNotEmpty() }
    }
