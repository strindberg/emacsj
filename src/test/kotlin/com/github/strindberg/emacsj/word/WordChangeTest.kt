package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val ACTION_CAPITAL_CASE = "com.github.strindberg.emacsj.actions.word.capitalcase"
private const val ACTION_UPPER_CASE = "com.github.strindberg.emacsj.actions.word.uppercase"
private const val ACTION_LOWER_CASE = "com.github.strindberg.emacsj.actions.word.lowercase"
private const val ACTION_CAPITAL_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.capitalcaseprevious"
private const val ACTION_UPPER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.uppercaseprevious"
private const val ACTION_LOWER_CASE_PREVIOUS = "com.github.strindberg.emacsj.actions.word.lowercaseprevious"
private const val ACTION_DELETE_NEXT_WORD = "com.github.strindberg.emacsj.actions.word.deletenextword"
private const val ACTION_DELETE_PREVIOUS_WORD = "com.github.strindberg.emacsj.actions.word.deletepreviousword"

@RunWith(JUnit4::class)
class WordChangeTest : BasePlatformTestCase() {

    @Test
    fun `capitalize word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    @Test
    fun `capitalize word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    @Test
    fun `capitalize word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `capitalize word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `capitalize word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bAr<caret>")
    }

    @Test
    fun `capitalize word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `capitalize word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    @Test
    fun `capitalize word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `capitalize word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult(" + - () Bar<caret>")
    }

    @Test
    fun `capitalize word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo - B<caret>ar")
    }

    @Test
    fun `capitalize word 12`() {
        myFixture.configureByText(FILE, "f<selection>oo foo b</selection><caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("fOo Foo B<caret>ar")
    }

    @Test
    fun `capitalize word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    @Test
    fun `capitalize word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo<caret>")
    }

    @Test
    fun `capitalize word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo (<caret>")
    }

    @Test
    fun `upper case word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `upper case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    @Test
    fun `upper case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `upper case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `upper case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bAR<caret>")
    }

    @Test
    fun `upper case word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `upper case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `upper case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `upper case word 08`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `upper case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `upper case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult(" + - () BAR<caret>")
    }

    @Test
    fun `upper case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO - B<caret>ar")
    }

    @Test
    fun `upper case word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret>Bar")
    }

    @Test
    fun `upper case word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO<caret>")
    }

    @Test
    fun `upper case word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO (<caret>")
    }

    @Test
    fun `lower case word 00`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `lower case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>O bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `lower case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `lower case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `lower case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>AR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `lower case word 05`() {
        myFixture.configureByText(FILE, "foo BAR<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `lower case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `lower case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `lower case word 08`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `lower case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `lower case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult(" + - () bar<caret>")
    }

    @Test
    fun `lower case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>FOO - B</selection>ar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo - b<caret>ar")
    }

    @Test
    fun `lower case word 20`() {
        myFixture.configureByText(FILE, "F<caret>OoBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    @Test
    fun `lower case word 30`() {
        myFixture.configureByText(FILE, "fOo<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `lower case word 31`() {
        myFixture.configureByText(FILE, "Foo (<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo (<caret>")
    }

    @Test
    fun `delete next word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret> bar")
    }

    @Test
    fun `delete next word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("fo<caret> bar")
    }

    @Test
    fun `delete next word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `delete next word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo <caret>")
    }

    @Test
    fun `delete next word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo b<caret>")
    }

    @Test
    fun `delete next word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `delete next word 06`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("")
    }

    @Test
    fun `delete next word 10`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret>Bar")
    }

    @Test
    fun `delete previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `delete previous word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>o bar")
    }

    @Test
    fun `delete previous word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret> bar")
    }

    @Test
    fun `delete previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>bar")
    }

    @Test
    fun `delete previous word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>ar")
    }

    @Test
    fun `delete previous word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>")
    }

    @Test
    fun `delete previous word 06`() {
        myFixture.configureByText(FILE, "BAR) () -<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("")
    }

    @Test
    fun `delete previous word 10`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>")
    }
}
