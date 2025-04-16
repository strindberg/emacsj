package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.actions.search.ReplaceAction
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.ActionPromoter
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.actions.BackspaceAction
import com.intellij.openapi.editor.actions.EnterAction
import com.intellij.openapi.editor.actions.PasteAction

internal class CommonActionsPromoter : ActionPromoter {

    override fun promote(actions: MutableList<out AnAction>, context: DataContext): List<AnAction> {
        val newList = actions.toMutableList()
        if (ISearchHandler.delegate != null) {
            newList.sortWith { a, b ->
                if (isISearchAction(a)) {
                    -1
                } else if (isISearchAction(b)) {
                    1
                } else {
                    0
                }
            }
        } else if (ReplaceHandler.delegate != null) {
            newList.sortWith { a, b ->
                if (a is ReplaceAction) {
                    -1
                } else if (b is ReplaceAction) {
                    1
                } else {
                    0
                }
            }
        } else if (UniversalArgumentHandler.delegate != null) {
            newList.sortWith { a, b ->
                if (a is EnterAction) {
                    -1
                } else if (b is EnterAction) {
                    1
                } else {
                    0
                }
            }
        }
        return newList
    }

    private fun isISearchAction(action: AnAction?) =
        action is ISearchAction || action is BackspaceAction || action is EnterAction || action is PasteAction
}
