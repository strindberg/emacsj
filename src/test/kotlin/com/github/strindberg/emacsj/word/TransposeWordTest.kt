package com.github.strindberg.emacsj.word

import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT0
import org.junit.jupiter.api.Test

private const val FILE = "transposewordfile.txt"

class TransposeWordTest : EmacsJTestCase() {

    @Test
    fun `Transpose 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `Transpose 01`() {
        myFixture.configureByText(FILE, " <caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar  foo<caret>")
    }

    @Test
    fun `Transpose 02`() {
        myFixture.configureByText(FILE, "<caret> foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar  foo<caret>")
    }

    @Test
    fun `Transpose 03`() {
        myFixture.configureByText(FILE, "f<caret>oo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `Transpose 04`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `Transpose 05`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `Transpose 06`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `Transpose 10`() {
        myFixture.configureByText(FILE, "f<caret>oo + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 11`() {
        myFixture.configureByText(FILE, "fo<caret>o + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 12`() {
        myFixture.configureByText(FILE, "foo<caret> + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 13`() {
        myFixture.configureByText(FILE, "foo <caret>+ bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 14`() {
        myFixture.configureByText(FILE, "foo +<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 15`() {
        myFixture.configureByText(FILE, "foo + <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `Transpose 16`() {
        myFixture.configureByText(FILE, "foo + b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + bar<caret>")
    }

    @Test
    fun `Transpose 17`() {
        myFixture.configureByText(FILE, "foo + bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + bar<caret>")
    }

    @Test
    fun `Transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `Transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `Transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `Transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.bar<caret>")
    }

    @Test
    fun `Transpose 31`() {
        myFixture.configureByText(FILE, "<caret><selection>foo b</selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    @Test
    fun `Transpose 32`() {
        myFixture.configureByText(FILE, "<selection>foo b<caret></selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    @Test
    fun `Transpose 41`() {
        myFixture.configureByText(FILE, "foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("barFoo<caret>")
    }

    @Test
    fun `Transpose 42`() {
        myFixture.configureByText(FILE, "F<caret>ooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    @Test
    fun `Transpose 43`() {
        myFixture.configureByText(FILE, "Foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    @Test
    fun `Reverse transpose 01`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `Reverse transpose 02`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `Reverse transpose 03`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `Reverse transpose 04`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `Reverse transpose 05`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `Reverse transpose 06`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `Reverse transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret> ")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar  foo")
    }

    @Test
    fun `Reverse transpose 08`() {
        myFixture.configureByText(FILE, "foo bar <caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar  foo")
    }

    @Test
    fun `Reverse transpose 10`() {
        myFixture.configureByText(FILE, "foo () b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

    @Test
    fun `Reverse transpose 11`() {
        myFixture.configureByText(FILE, "foo () <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

    @Test
    fun `Reverse transpose 12`() {
        myFixture.configureByText(FILE, "foo ()<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

    @Test
    fun `Reverse transpose 13`() {
        myFixture.configureByText(FILE, "baz foo (<caret>) bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("baz <caret>bar () foo")
    }

    @Test
    fun `Reverse transpose 14`() {
        myFixture.configureByText(FILE, "baz () foo <caret>() bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("baz () <caret>bar () foo")
    }

    @Test
    fun `Reverse transpose 15`() {
        myFixture.configureByText(FILE, "fo<caret>o () bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo () bar")
    }

    @Test
    fun `Reverse transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo.bar")
    }

    @Test
    fun `Reverse transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

    @Test
    fun `Reverse transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

    @Test
    fun `Reverse transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

    @Test
    fun `Reverse transpose 31`() {
        myFixture.configureByText(FILE, "fo<selection><caret>o bar</selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    @Test
    fun `Reverse transpose 32`() {
        myFixture.configureByText(FILE, "fo<selection>o bar<caret></selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    @Test
    fun `Reverse transpose 41`() {
        myFixture.configureByText(FILE, "fooBa<caret>r")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>barFoo")
    }

    @Test
    fun `Reverse transpose 42`() {
        myFixture.configureByText(FILE, "BazFoo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("Baz<caret>BarFoo")
    }

    @Test
    fun `Reverse transpose 43`() {
        myFixture.configureByText(FILE, "FooB<caret>ar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>BarFoo")
    }

    @Test
    fun `Reverse transpose 44`() {
        myFixture.configureByText(FILE, "foo bar<caret> baz")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo <caret>baz bar")
    }

    @Test
    fun `Mark Transpose 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar baz")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        repeat(3) {
            myFixture.performEditorAction(ACTION_NEXT_WORD)
        }
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)

        myFixture.checkResult("<caret>baz bar foo")
    }

    @Test
    fun `Mark Transpose 01`() {
        myFixture.configureByText(FILE, "foo bar baz<caret>")

        myFixture.performEditorAction(ACTION_PUSH_MARK)
        myFixture.performEditorAction(ACTION_PUSH_MARK)
        repeat(3) {
            myFixture.performEditorAction(ACTION_PREVIOUS_WORD)
        }
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)

        myFixture.checkResult("baz bar foo<caret>")
    }

    @Test
    fun `Transpose words works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |foo <caret>bar
                |baz <caret>qux
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)

        myFixture.checkResult(
            """
                |bar foo<caret>
                |qux baz<caret>
            """.trimMargin()
        )
    }
}
