package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.search.ReplaceAction
import com.github.strindberg.emacsj.actions.universal.RepeatAction
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.actions.BackspaceAction
import com.intellij.openapi.editor.actions.EnterAction

internal class CommonActionsPromoter : ActionPromoter {

    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
        actions.toMutableList().apply {
            when {
                ISearchHandler.delegate != null -> {
                    sortByDescending { isISearchAction(it) }
                }
                ReplaceHandler.delegate != null -> {
                    sortByDescending { it is ReplaceAction }
                }
                UniversalArgumentHandler.delegate != null -> {
                    sortByDescending { it is EnterAction }
                }
                UniversalArgumentHandler.repeating -> {
                    sortByDescending { it is RepeatAction }
                }
            }
        }

    private fun isISearchAction(action: AnAction?) =
        action is ISearchAction ||
            action is BackspaceAction ||
            action is EnterAction ||
            action is com.intellij.openapi.editor.actions.PasteAction ||
            action is com.github.strindberg.emacsj.actions.paste.PasteAction
}
