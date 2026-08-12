package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.actions.mark.PushMarkAction
import com.github.strindberg.emacsj.actions.search.ISearchRegexpForwardAction
import com.github.strindberg.emacsj.actions.search.ISearchTextForwardAction
import com.github.strindberg.emacsj.actions.search.ReplaceNewLineAction
import com.github.strindberg.emacsj.actions.universal.CancelRepeatAction
import com.github.strindberg.emacsj.actions.zap.ZapToCharAction
import com.github.strindberg.emacsj.search.ISearchDelegate
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceDelegate
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.search.SearchDirection
import com.github.strindberg.emacsj.search.SearchType
import com.github.strindberg.emacsj.ui.EmacsJActionsPromoter
import com.intellij.openapi.actionSystem.DataContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private const val FILE = "promoterfile.txt"

class EmacsJActionsPromoterTest : EmacsJTestCase() {

    @Test
    fun `Promoter sorts ISearch actions first when ISearch is active`() {
        myFixture.configureByText(FILE, "")
        ISearchHandler.delegate = ISearchDelegate(
            editor = myFixture.editor,
            project = project,
            searchType = SearchType.TEXT,
            direction = SearchDirection.FORWARD
        )

        val isearch1 = ISearchTextForwardAction()
        val isearch2 = ISearchRegexpForwardAction()
        val cancelRepeat = CancelRepeatAction()
        val replace = ReplaceNewLineAction()

        val actions = setOf(isearch1, isearch2, cancelRepeat, replace, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = EmacsJActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertTrue((sorted[0] == isearch1 || sorted[0] == isearch2) && (sorted[5] == cancelRepeat || sorted[5] == replace))
        }
    }

    @Test
    fun `Promoter sorts Replace actions first when Replace is active`() {
        myFixture.configureByText(FILE, "")
        ReplaceHandler.delegate = ReplaceDelegate(
            editor = myFixture.editor,
            project = project,
            type = SearchType.TEXT,
            selection = null,
            lastSearch = null
        )

        val replace = ReplaceNewLineAction()
        val isearch = ISearchTextForwardAction()
        val cancelRepeat = CancelRepeatAction()
        val actions = setOf(replace, isearch, cancelRepeat, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = EmacsJActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertTrue(replace == sorted[0] && (sorted[4] == cancelRepeat || sorted[4] == isearch))
        }
    }

    @Test
    fun `Promoter sorts Repeat actions first when repeating`() {
        myFixture.configureByText(FILE, "")
        EmacsJService.instance.setRepeating(true)

        val cancel = CancelRepeatAction()
        val replace = ReplaceNewLineAction()
        val isearch = ISearchTextForwardAction()
        val actions = setOf(cancel, replace, isearch, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = EmacsJActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertTrue(cancel == sorted[0] && (sorted[4] == replace || sorted[4] == isearch))
        }
    }

    @Test
    fun `Promoter sorts EmacsJ actions last when no ui is active`() {
        myFixture.configureByText(FILE, "")

        val cancel = CancelRepeatAction()
        val replace = ReplaceNewLineAction()
        val isearch = ISearchTextForwardAction()
        val push = PushMarkAction()
        val zap = ZapToCharAction()
        val actions = setOf(cancel, replace, isearch, push, zap)

        allPermutations(actions).forEach { actionList ->
            val sorted = EmacsJActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertTrue((push == sorted[0] || zap == sorted[0]) && (sorted[4] == replace || sorted[4] == isearch || sorted[4] == cancel))
        }
    }
}

private fun <T> allPermutations(set: Set<T>): Set<List<T>> {
    if (set.isEmpty()) return emptySet()

    fun <T> permutations(list: List<T>): Set<List<T>> {
        if (list.isEmpty()) return setOf(emptyList())

        val result: MutableSet<List<T>> = mutableSetOf()
        for (i in list.indices) {
            permutations(list - list[i]).forEach { item ->
                result.add(item + list[i])
            }
        }
        return result
    }

    return permutations(set.toList())
}
