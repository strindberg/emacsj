package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.search.ReplaceAction
import com.github.strindberg.emacsj.actions.universal.RepeatAction
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext

internal class EmacsJActionsPromoter : ActionPromoter {

    override fun promote(actions: List<AnAction>, context: DataContext): List<AnAction> =
        actions.toMutableList().apply {
            when {
                ISearchHandler.delegate != null -> {
                    sortByDescending { it is ISearchAction }
                }
                ReplaceHandler.delegate != null -> {
                    sortByDescending { it is ReplaceAction }
                }
                EmacsJService.instance.isRepeating() -> {
                    sortByDescending { it is RepeatAction }
                }
                else -> {
                    sortBy { it is ISearchAction || it is ReplaceAction || it is RepeatAction }
                }
            }
        }
}
