package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.universal.UniversalArgumentAction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ISearchState
import com.github.strindberg.emacsj.ui.EmacsJTypedActionService
import com.github.strindberg.emacsj.universal.UniversalArgumentDelegate
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.xref.XRefHandler
import com.github.strindberg.emacsj.xref.XRefHandler.Companion.pushPlace
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger

internal class EmacsJActionListener : AnActionListener {

    private var universalArgumentDelegate: UniversalArgumentDelegate? = null

    init {
        // Installs the raw typed-action handler that routes keystrokes into the isearch, zap and universal-argument delegates.
        ApplicationManager.getApplication().service<EmacsJTypedActionService>()
    }

    @Suppress("TooGenericExceptionCaught")
    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
        EmacsJService.instance.setPerformingAction(true)

        try {
            if (ActionManager.getInstance().getId(action) in XRefHandler.xRefActionIds) {
                event.project?.pushPlace()
            }

            ISearchHandler.delegate?.let { delegate ->
                if (delegate.state != ISearchState.EDIT && action !is ISearchAction) {
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
        } catch (e: Exception) {
            thisLogger().error(e)
        }
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        try {
            val actionId = ActionManager.getInstance().getId(action)

            if (result.isPerformed && actionId != null) {
                EmacsJService.instance.addAction(actionId)
            }

            universalArgumentDelegate?.let { delegate ->
                actionId?.let { delegate.repeatAction(it) }
                universalArgumentDelegate = null
            }
        } finally {
            EmacsJService.instance.setPerformingAction(false)
        }
    }
}
