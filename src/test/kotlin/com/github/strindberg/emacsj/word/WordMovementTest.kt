package com.github.strindberg.emacsj.word

import com.github.strindberg.emacsj.EmacsJTestCase
import org.junit.jupiter.api.Test

private const val FILE = "wordmovementfile.txt"

class WordMovementTest : EmacsJTestCase() {

    @Test
    fun `Next word 00`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Next word 01`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Next word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Next word 03`() {
        myFixture.configureByText(FILE, "<caret> foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>")
    }

    @Test
    fun `Next word 04`() {
        myFixture.configureByText(FILE, "<caret>+ (foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("+ (foo<caret>")
    }

    @Test
    fun `Next word 05`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    @Test
    fun `Next word 06`() {
        myFixture.configureByText(FILE, "<caret> fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>Bar")
    }

    @Test
    fun `Next word 07`() {
        myFixture.configureByText(FILE, "<caret>)<fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(")<foo<caret>Bar")
    }

    @Test
    fun `Next word 08`() {
        myFixture.configureByText(FILE, "foo<caret>BarBaz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBar<caret>Baz")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBarBaz<caret>")
    }

    @Test
    fun `Previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Previous word 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Previous word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `Previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo ")
    }

    @Test
    fun `Previous word 04`() {
        myFixture.configureByText(FILE, "foo) ()<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo) ()")
    }

    @Test
    fun `Previous word 05`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    @Test
    fun `Previous word 06`() {
        myFixture.configureByText(FILE, "fooBar <caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar ")
    }

    @Test
    fun `Previous word 07`() {
        myFixture.configureByText(FILE, "fooBar$(<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar$(")
    }

    @Test
    fun `Previous word 08`() {
        myFixture.configureByText(FILE, "fooBar<caret>Baz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>BarBaz")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>fooBarBaz")
    }

    @Test
    fun `Move to next word works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar
                |<caret>baz qux
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_NEXT_WORD)

        myFixture.checkResult(
            """
                |foo<caret> bar
                |baz<caret> qux
            """.trimMargin()
        )
    }

    @Test
    fun `Move to previous word works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |foo bar<caret>
                |baz qux<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)

        myFixture.checkResult(
            """
                |foo <caret>bar
                |baz <caret>qux
            """.trimMargin()
        )
    }
}
