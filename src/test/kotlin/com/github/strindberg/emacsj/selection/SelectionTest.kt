package com.github.strindberg.emacsj.selection

import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.intellij.openapi.editor.ex.EditorEx
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

private const val FILE = "selectionfile.txt"

class SelectionTest : EmacsJTestCase() {

    @Test
    fun `Select next occurrence selects the next occurrence of the selection`() {
        myFixture.configureByText(FILE, "<selection>foo<caret></selection> bar foo")

        myFixture.performEditorAction(ACTION_SELECT_NEXT_OCCURRENCE)

        myFixture.checkResult("<selection>foo<caret></selection> bar <selection>foo<caret></selection>")
    }

    @Test
    fun `Select next occurrence turns off sticky selection and keeps its selection`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo")
        // Part of a word, so that falling back to the word at the caret would show.
        startStickySelection(to = 2)

        myFixture.performEditorAction(ACTION_SELECT_NEXT_OCCURRENCE)

        assertFalse(editor().isStickySelection)
        myFixture.checkResult("<selection>fo<caret></selection>o bar <selection>fo<caret></selection>o")
    }

    @Test
    fun `Select next occurrence under sticky selection with nothing selected selects the word at the caret`() {
        myFixture.configureByText(FILE, "f<caret>oo bar foo")
        editor().isStickySelection = true

        myFixture.performEditorAction(ACTION_SELECT_NEXT_OCCURRENCE)

        assertFalse(editor().isStickySelection)
        myFixture.checkResult("<selection>f<caret>oo</selection> bar foo")
    }

    @Test
    fun `Select all occurrences turns off sticky selection and selects every occurrence`() {
        myFixture.configureByText(FILE, "<caret>foo bar foo baz foo")
        startStickySelection(to = 3)

        myFixture.performEditorAction(ACTION_SELECT_ALL_OCCURRENCES)

        assertFalse(editor().isStickySelection)
        myFixture.checkResult(
            "<selection>foo<caret></selection> bar <selection>foo<caret></selection> baz <selection>foo<caret></selection>"
        )
    }

    @Test
    fun `Universal argument selects that many next occurrences`() {
        myFixture.configureByText(FILE, "<selection>foo<caret></selection> foo foo foo")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.performEditorAction(ACTION_SELECT_NEXT_OCCURRENCE)

        myFixture.checkResult(
            "<selection>foo<caret></selection> <selection>foo<caret></selection> <selection>foo<caret></selection> foo"
        )
    }

    private fun editor(): EditorEx = myFixture.editor as EditorEx

    /** Starts sticky selection at the caret, as Set Mark does, and extends it by moving the caret to [to]. */
    private fun startStickySelection(to: Int) {
        editor().isStickySelection = true
        editor().caretModel.moveToOffset(to)
    }
}
