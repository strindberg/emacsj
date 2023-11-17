package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_CAPITAL_CASE = "com.github.strindberg.emacsj.actions.word.capitalcase"
private const val ACTION_UPPER_CASE = "com.github.strindberg.emacsj.actions.word.uppercase"
private const val ACTION_LOWER_CASE = "com.github.strindberg.emacsj.actions.word.lowercase"
private const val ACTION_CAPITAL_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.capitalcaseprevious"
private const val ACTION_UPPER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.uppercaseprevious"
private const val ACTION_LOWER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.lowercaseprevious"
private const val ACTION_DELETE_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.deletenextword"
private const val ACTION_DELETE_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.deletepreviousword"

class WordChangeTest : BasePlatformTestCase() {

    fun `test Capitalize word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    fun `test Capitalize word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    fun `test Capitalize word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    fun `test Capitalize word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    fun `test Capitalize word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bAr<caret>")
    }

    fun `test Capitalize word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Capitalize word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    fun `test Capitalize word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    fun `test Capitalize word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult(" + - () Bar<caret>")
    }

    fun `test Capitalize word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo - B<caret>ar")
    }

    fun `test Capitalize word 12`() {
        myFixture.configureByText(FILE, "f<selection>oo foo b</selection><caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("fOo Foo B<caret>ar")
    }

    fun `test Capitalize word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    fun `test Capitalize word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo<caret>")
    }

    fun `test Capitalize word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo (<caret>")
    }

    fun `test Upper case word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    fun `test Upper case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    fun `test Upper case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    fun `test Upper case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    fun `test Upper case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bAR<caret>")
    }

    fun `test Upper case word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Upper case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    fun `test Upper case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    fun `test Upper case word 08`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    fun `test Upper case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    fun `test Upper case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult(" + - () BAR<caret>")
    }

    fun `test Upper case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO - B<caret>ar")
    }

    fun `test Upper case word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret>Bar")
    }

    fun `test Upper case word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO<caret>")
    }

    fun `test Upper case word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO (<caret>")
    }

    fun `test Lower case word 00`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Lower case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>O bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Lower case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Lower case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Lower case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>AR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Lower case word 05`() {
        myFixture.configureByText(FILE, "foo BAR<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    fun `test Lower case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Lower case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Lower case word 08`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Lower case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Lower case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult(" + - () bar<caret>")
    }

    fun `test Lower case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>FOO - B</selection>ar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo - b<caret>ar")
    }

    fun `test Lower case word 20`() {
        myFixture.configureByText(FILE, "F<caret>OoBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    fun `test Lower case word 30`() {
        myFixture.configureByText(FILE, "fOo<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Lower case word 31`() {
        myFixture.configureByText(FILE, "Foo (<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo (<caret>")
    }

    fun `test Delete next word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret> bar")
    }

    fun `test Delete next word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("fo<caret> bar")
    }

    fun `test Delete next word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    fun `test Delete next word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo <caret>")
    }

    fun `test Delete next word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo b<caret>")
    }

    fun `test Delete next word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Delete next word 06`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("")
    }

    fun `test Delete next word 10`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret>Bar")
    }

    fun `test Delete previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo bar")
    }

    fun `test Delete previous word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>o bar")
    }

    fun `test Delete previous word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret> bar")
    }

    fun `test Delete previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>bar")
    }

    fun `test Delete previous word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>ar")
    }

    fun `test Delete previous word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>")
    }

    fun `test Delete previous word 06`() {
        myFixture.configureByText(FILE, "BAR) () -<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("")
    }

    fun `test Delete previous word 10`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>")
    }
}
