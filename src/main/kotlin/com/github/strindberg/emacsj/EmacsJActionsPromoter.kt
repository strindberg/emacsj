package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.search.ReplaceAction
import com.github.strindberg.emacsj.actions.universal.RepeatAction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.actions.BackspaceAction
import com.intellij.openapi.editor.actions.EnterAction
import com.intellij.openapi.editor.actions.PasteAction

internal class EmacsJActionsPromoter : ActionPromoter {

    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
        actions.toMutableList().apply {
            when {
                ISearchHandler.delegate != null -> {
                    sortByDescending { it.isISearchAction() }
                }
                ReplaceHandler.delegate != null -> {
                    sortByDescending { it is ReplaceAction }
                }
                UniversalArgumentHandler.repeating -> {
                    sortByDescending { it is RepeatAction }
                }
                UniversalArgumentHandler.delegate != null -> {
                    sortByDescending { it is EnterAction }
                }
                else -> {
                    sortBy { it is ISearchAction || it is ReplaceAction || it is RepeatAction }
                }
            }
        }

    private fun AnAction.isISearchAction() =
        this is ISearchAction ||
            this is BackspaceAction ||
            this is EnterAction ||
            this is PasteAction ||
            this is com.github.strindberg.emacsj.actions.paste.PasteAction
}
