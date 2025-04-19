package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class WordMovementTest : BasePlatformTestCase() {

    fun `test Next word 00`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Next word 01`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Next word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Next word 03`() {
        myFixture.configureByText(FILE, "<caret> foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>")
    }

    fun `test Next word 04`() {
        myFixture.configureByText(FILE, "<caret>+ (foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("+ (foo<caret>")
    }

    fun `test Next word 05`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    fun `test Next word 06`() {
        myFixture.configureByText(FILE, "<caret> fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>Bar")
    }

    fun `test Next word 07`() {
        myFixture.configureByText(FILE, "<caret>)<fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(")<foo<caret>Bar")
    }

    fun `test Next word 08`() {
        myFixture.configureByText(FILE, "foo<caret>BarBaz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBar<caret>Baz")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBarBaz<caret>")
    }

    fun `test Previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Previous word 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Previous word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    fun `test Previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo ")
    }

    fun `test Previous word 04`() {
        myFixture.configureByText(FILE, "foo) ()<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo) ()")
    }

    fun `test Previous word 05`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    fun `test Previous word 06`() {
        myFixture.configureByText(FILE, "fooBar <caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar ")
    }

    fun `test Previous word 07`() {
        myFixture.configureByText(FILE, "fooBar$(<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar$(")
    }

    fun `test Previous word 08`() {
        myFixture.configureByText(FILE, "fooBar<caret>Baz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>BarBaz")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>fooBarBaz")
    }
}
