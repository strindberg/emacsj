package com.github.strindberg.emacsj.word

import com.github.strindberg.emacsj.EmacsJTestCase
import org.junit.jupiter.api.Test

private const val FILE = "wordchangefile.txt"

class WordChangeTest : EmacsJTestCase() {

    @Test
    fun `Capitalize word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    @Test
    fun `Capitalize word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    @Test
    fun `Capitalize word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `Capitalize word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `Capitalize word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bAr<caret>")
    }

    @Test
    fun `Capitalize word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Capitalize word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret> bar")
    }

    @Test
    fun `Capitalize word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("foo Bar<caret>")
    }

    @Test
    fun `Capitalize word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult(" + - () Bar<caret>")
    }

    @Test
    fun `Capitalize word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo - B<caret>ar")
    }

    @Test
    fun `Capitalize word 12`() {
        myFixture.configureByText(FILE, "f<selection>oo foo b</selection><caret>ar")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("fOo Foo B<caret>ar")
    }

    @Test
    fun `Capitalize word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_CAPITAL_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    @Test
    fun `Capitalize word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo<caret>")
    }

    @Test
    fun `Capitalize word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_CAPITAL_CASE_PREVIOUS)
        myFixture.checkResult("Foo (<caret>")
    }

    @Test
    fun `Upper case word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `Upper case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foO<caret> bar")
    }

    @Test
    fun `Upper case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `Upper case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `Upper case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bAR<caret>")
    }

    @Test
    fun `Upper case word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Upper case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `Upper case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `Upper case word 08`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret> bar")
    }

    @Test
    fun `Upper case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `Upper case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () bar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult(" + - () BAR<caret>")
    }

    @Test
    fun `Upper case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>foo - b</selection>ar")
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO - B<caret>ar")
    }

    @Test
    fun `Upper case word 20`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_UPPER_CASE)
        myFixture.checkResult("FOO<caret>Bar")
    }

    @Test
    fun `Upper case word 30`() {
        myFixture.configureByText(FILE, "foo<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO<caret>")
    }

    @Test
    fun `Upper case word 31`() {
        myFixture.configureByText(FILE, "foo (<caret>")
        myFixture.performEditorAction(ACTION_UPPER_CASE_PREVIOUS)
        myFixture.checkResult("FOO (<caret>")
    }

    @Test
    fun `Lower case word 00`() {
        myFixture.configureByText(FILE, "<caret>FOO bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `Lower case word 01`() {
        myFixture.configureByText(FILE, "fo<caret>O bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `Lower case word 02`() {
        myFixture.configureByText(FILE, "foo<caret> BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Lower case word 03`() {
        myFixture.configureByText(FILE, "foo <caret>BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Lower case word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>AR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Lower case word 05`() {
        myFixture.configureByText(FILE, "foo BAR<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo BAR<caret>")
    }

    @Test
    fun `Lower case word 06`() {
        myFixture.configureByText(FILE, "<caret>Foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `Lower case word 07`() {
        myFixture.configureByText(FILE, "foo <caret>Bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Lower case word 08`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `Lower case word 09`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Lower case word 10`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult(" + - () bar<caret>")
    }

    @Test
    fun `Lower case word 11`() {
        myFixture.configureByText(FILE, "<selection><caret>FOO - B</selection>ar")
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("foo - b<caret>ar")
    }

    @Test
    fun `Lower case word 20`() {
        myFixture.configureByText(FILE, "F<caret>OoBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_LOWER_CASE)
        myFixture.checkResult("Foo<caret>Bar")
    }

    @Test
    fun `Lower case word 30`() {
        myFixture.configureByText(FILE, "fOo<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Lower case word 31`() {
        myFixture.configureByText(FILE, "Foo (<caret>")
        myFixture.performEditorAction(ACTION_LOWER_CASE_PREVIOUS)
        myFixture.checkResult("foo (<caret>")
    }

    @Test
    fun `Delete next word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret> bar")
    }

    @Test
    fun `Delete next word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("fo<caret> bar")
    }

    @Test
    fun `Delete next word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Delete next word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo <caret>")
    }

    @Test
    fun `Delete next word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo b<caret>")
    }

    @Test
    fun `Delete next word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Delete next word 06`() {
        myFixture.configureByText(FILE, "<caret> + - () BAR")
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("")
    }

    @Test
    fun `Delete next word 10`() {
        myFixture.configureByText(FILE, "<caret>fooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult("<caret>Bar")
    }

    @Test
    fun `Delete next word works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>fooBar
                |<caret>pooBear
            """.trimMargin()
        )
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_NEXT_WORD)
        myFixture.checkResult(
            """
                |<caret>Bar
                |<caret>Bear
            """.trimMargin()
        )
    }

    @Test
    fun `Delete previous word 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `Delete previous word 01`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>o bar")
    }

    @Test
    fun `Delete previous word 02`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret> bar")
    }

    @Test
    fun `Delete previous word 03`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("<caret>bar")
    }

    @Test
    fun `Delete previous word 04`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>ar")
    }

    @Test
    fun `Delete previous word 05`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo <caret>")
    }

    @Test
    fun `Delete previous word 06`() {
        myFixture.configureByText(FILE, "BAR) () -<caret>")
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("")
    }

    @Test
    fun `Delete previous word 10`() {
        myFixture.configureByText(FILE, "fooBar<caret>")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult("foo<caret>")
    }

    @Test
    fun `Delete previous word works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |fooBar<caret>
                |pooBear<caret>
            """.trimMargin()
        )
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_DELETE_PREVIOUS_WORD)
        myFixture.checkResult(
            """
                |foo<caret>
                |poo<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Upper case works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar
                |<caret>baz qux
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UPPER_CASE)

        myFixture.checkResult(
            """
                |FOO<caret> bar
                |BAZ<caret> qux
            """.trimMargin()
        )
    }

    @Test
    fun `Capital case works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>foo bar
                |<caret>baz qux
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_CAPITAL_CASE)

        myFixture.checkResult(
            """
                |Foo<caret> bar
                |Baz<caret> qux
            """.trimMargin()
        )
    }
}
