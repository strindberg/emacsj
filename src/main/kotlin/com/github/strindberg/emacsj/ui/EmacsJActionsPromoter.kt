package com.github.strindberg.emacsj.ui

import com.github.strindberg.emacsj.EmacsJService
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
                    sortByDescending {
                        when (it) {
                            is ISearchAction -> 1
                            is ReplaceAction, is RepeatAction -> -1
                            else -> 0
                        }
                    }
                }
                ReplaceHandler.delegate != null -> {
                    sortByDescending {
                        when (it) {
                            is ReplaceAction -> 1
                            is ISearchAction, is RepeatAction -> -1
                            else -> 0
                        }
                    }
                }
                EmacsJService.instance.isRepeating() -> {
                    sortByDescending {
                        when (it) {
                            is RepeatAction -> 1
                            is ISearchAction, is ReplaceAction -> -1
                            else -> 0
                        }
                    }
                }
                else -> {
                    sortBy { it is ISearchAction || it is ReplaceAction || it is RepeatAction }
                }
            }
        }
}
