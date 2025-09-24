package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.actions.mark.PushMarkAction
import com.github.strindberg.emacsj.actions.search.ISearchRegexpForwardAction
import com.github.strindberg.emacsj.actions.search.ISearchTextForwardAction
import com.github.strindberg.emacsj.actions.search.ReplaceNewLineAction
import com.github.strindberg.emacsj.actions.universal.CancelRepeatAction
import com.github.strindberg.emacsj.actions.zap.ZapToCharAction
import com.github.strindberg.emacsj.universal.UniversalArgumentDelegate
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.actions.EnterAction
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CommonActionsPromoterTest : BasePlatformTestCase() {

    override fun tearDown() {
        ISearchHandler.delegate?.hide()
        ReplaceHandler.delegate?.hide()
        UniversalArgumentHandler.delegate?.hide()
        UniversalArgumentHandler.repeating = false
        super.tearDown()
    }

    fun `test Promoter sorts ISearch actions first when ISearch is active`() {
        myFixture.configureByText(FILE, "")
        ISearchHandler.delegate = ISearchDelegate(myFixture.editor, SearchType.TEXT, Direction.FORWARD)

        val isearch1 = ISearchTextForwardAction()
        val isearch2 = ISearchRegexpForwardAction()
        val actions = setOf(isearch1, isearch2, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = CommonActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertTrue(sorted[0] == isearch1 || sorted[0] == isearch2)
        }
    }

    fun `test Promoter sorts Replace actions first when Replace is active`() {
        myFixture.configureByText(FILE, "")
        ReplaceHandler.delegate = ReplaceDelegate(myFixture.editor, SearchType.TEXT, null, null)

        val replace = ReplaceNewLineAction()
        val actions = setOf(replace, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = CommonActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertEquals(replace, sorted[0])
        }
    }

    fun `test Promoter sorts Enter action first when Universal Argument is active`() {
        myFixture.configureByText(FILE, "")
        UniversalArgumentHandler.delegate = UniversalArgumentDelegate(myFixture.editor, null)

        val enter = EnterAction()
        val actions = setOf(enter, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = CommonActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertEquals(enter, sorted[0])
        }
    }

    fun `test Promoter sorts Repeat actions first when repeating`() {
        myFixture.configureByText(FILE, "")
        UniversalArgumentHandler.repeating = true

        val cancel = CancelRepeatAction()
        val actions = setOf(cancel, PushMarkAction(), ZapToCharAction())

        allPermutations(actions).forEach { actionList ->
            val sorted = CommonActionsPromoter().promote(actionList, DataContext.EMPTY_CONTEXT)
            assertEquals(actions.size, sorted.size)
            assertEquals(cancel, sorted[0])
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
