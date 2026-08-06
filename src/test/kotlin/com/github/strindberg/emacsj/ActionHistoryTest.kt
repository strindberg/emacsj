package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.view.ACTION_RECENTER
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN

private const val FILE = "actionhistoryfile.txt"

private const val SENTINEL = "sentinel"

class ActionHistoryTest : EmacsJTestCase() {

    fun `test An action is recorded once, by id`() {
        myFixture.configureByText(FILE, "<caret>foo")
        EmacsJService.instance.addAction(SENTINEL)

        myFixture.performEditorAction(ACTION_RECENTER)

        // An editor action raises a command too. If both listeners recorded it, the command name would have pushed
        // the sentinel out of the previous slot.
        assertEquals(ACTION_RECENTER, EmacsJService.instance.lastActionIds().last)
        assertEquals(SENTINEL, EmacsJService.instance.lastActionIds().previous)
    }

    fun `test A platform action is recorded by its id`() {
        myFixture.configureByText(FILE, "<caret>foo\nbar")

        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_DOWN)

        assertEquals(ACTION_EDITOR_MOVE_CARET_DOWN, EmacsJService.instance.lastActionId())
    }

    fun `test Typing is recorded, but never as an action id`() {
        myFixture.configureByText(FILE, "<caret>foo")
        EmacsJService.instance.addAction(ACTION_RECENTER)

        myFixture.type("x")

        // Typing raises a command but no action, so it has to arrive through the command listener. What matters is
        // that it displaces the previous action: the id must no longer be the last thing recorded.
        assertNotSame(ACTION_RECENTER, EmacsJService.instance.lastActionId())
        assertNotNull(EmacsJService.instance.lastActionId())
    }
}
