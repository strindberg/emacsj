package com.github.strindberg.emacsj.ui

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.universal.UniversalArgumentAction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentDelegate
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service

internal class EmacsJCancelListener : AnActionListener {

    private var universalArgumentDelegate: UniversalArgumentDelegate? = null

    init {
        ApplicationManager.getApplication().service<EmacsJTypedActionService>()
    }

    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        ISearchHandler.delegate?.let { delegate ->
            if (action !is ISearchAction) {
                delegate.hide()
            }
        }
        UniversalArgumentHandler.delegate?.let { delegate ->
            if (action !is UniversalArgumentAction) {
                universalArgumentDelegate = delegate
                delegate.hide()
            }
        }
        ZapHandler.delegate?.hide()
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        universalArgumentDelegate?.let { delegate ->
            ActionManager.getInstance().getId(action)?.let { actionId ->
                delegate.repeatAction(actionId)
            }
            universalArgumentDelegate = null
        }
    }
}
