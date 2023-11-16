package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.movenextword"
private const val ACTION_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.movepreviousword"

class WordMovementTest : BasePlatformTestCase() {

    fun `testNext word 00`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `testNext word 01`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `testNext word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `testNext word 03`() {
        myFixture.configureByText(FILE, "<caret> foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>")
    }

    fun `testNext word 04`() {
        myFixture.configureByText(FILE, "<caret>+ (foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("+ (foo<caret>")
    }

    fun `testNext word 05`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    fun `testNext word 06`() {
        myFixture.configureByText(FILE, "<caret> fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>Bar")
    }

    fun `testNext word 07`() {
        myFixture.configureByText(FILE, "<caret>)<fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(")<foo<caret>Bar")
    }

    fun `testNext word 08`() {
        myFixture.configureByText(FILE, "foo<caret>BarBaz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBar<caret>Baz")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBarBaz<caret>")
    }

    fun `testPrevious word 00`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `testPrevious word 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `testPrevious word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `testPrevious word 03`() {
        myFixture.configureByText(FILE, "foo <caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo ")
    }

    fun `testPrevious word 04`() {
        myFixture.configureByText(FILE, "foo) ()<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo) ()")
    }

    fun `testPrevious word 05`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    fun `testPrevious word 06`() {
        myFixture.configureByText(FILE, "fooBar <caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar ")
    }

    fun `testPrevious word 07`() {
        myFixture.configureByText(FILE, "fooBar$(<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar$(")
    }

    fun `testPrevious word 08`() {
        myFixture.configureByText(FILE, "fooBar<caret>Baz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>BarBaz")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>fooBarBaz")
    }
}
