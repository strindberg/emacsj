package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val ACTION_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.movenextword"
private const val ACTION_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.movepreviousword"

@RunWith(JUnit4::class)
class WordMovementTest : BasePlatformTestCase() {

    @Test
    fun `next word 00`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `next word 01`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `next word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `next word 03`() {
        myFixture.configureByText(FILE, "<caret> foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>")
    }

    @Test
    fun `next word 04`() {
        myFixture.configureByText(FILE, "<caret>+ (foo")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("+ (foo<caret>")
    }

    @Test
    fun `next word 05`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    @Test
    fun `next word 06`() {
        myFixture.configureByText(FILE, "<caret> fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(" foo<caret>Bar")
    }

    @Test
    fun `next word 07`() {
        myFixture.configureByText(FILE, "<caret>)<fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult(")<foo<caret>Bar")
    }

    @Test
    fun `next word 08`() {
        myFixture.configureByText(FILE, "foo<caret>BarBaz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBar<caret>Baz")
        myFixture.performEditorAction(ACTION_NEXT_WORD)
        myFixture.checkResult("fooBarBaz<caret>")
    }

    @Test
    fun `previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `previous word 01`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `previous word 02`() {
        myFixture.configureByText(FILE, "f<caret>oo")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo")
    }

    @Test
    fun `previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo ")
    }

    @Test
    fun `previous word 04`() {
        myFixture.configureByText(FILE, "foo) ()<caret>")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo) ()")
    }

    @Test
    fun `previous word 05`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar")
    }

    @Test
    fun `previous word 06`() {
        myFixture.configureByText(FILE, "fooBar <caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar ")
    }

    @Test
    fun `previous word 07`() {
        myFixture.configureByText(FILE, "fooBar$(<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>Bar$(")
    }

    @Test
    fun `previous word 08`() {
        myFixture.configureByText(FILE, "fooBar<caret>Baz")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>BarBaz")
        myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        myFixture.checkResult("<caret>fooBarBaz")
    }
}
