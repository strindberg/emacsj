package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.universal.UniversalArgumentAction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentDelegate
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction

internal class EmacsJCancelListener : AnActionListener {

    private var universalArgumentDelegate: UniversalArgumentDelegate? = null

    init {
        TypedAction.getInstance().apply {
            setupRawHandler(
                object : TypedActionHandlerBase(rawHandler) {
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
            )
        }
    }

    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        ISearchHandler.delegate?.let { delegate ->
            if (action !is ISearchAction) {
                delegate.cancel()
            }
        }
        UniversalArgumentHandler.delegate?.let { delegate ->
            if (action !is UniversalArgumentAction) {
                delegate.cancel()
                universalArgumentDelegate = delegate
            }
        }
        ZapHandler.delegate?.cancel()
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        universalArgumentDelegate?.let { delegate ->
            ActionManager.getInstance().getId(action)?.let { actionId ->
                delegate.repeatAction(actionId)
            }
        }
        universalArgumentDelegate = null
    }
}
