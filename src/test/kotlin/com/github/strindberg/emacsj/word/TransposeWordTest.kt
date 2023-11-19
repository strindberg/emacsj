package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewords"
private const val ACTION_REVERSE_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewordsreverse"

class TransposeWordTest : BasePlatformTestCase() {

    fun `test Transpose 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    fun `test Transpose 01`() {
        myFixture.configureByText(FILE, " <caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult(" <caret>foo bar")
    }

    fun `test Transpose 02`() {
        myFixture.configureByText(FILE, "<caret> foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret> foo bar")
    }

    fun `test Transpose 03`() {
        myFixture.configureByText(FILE, "f<caret>oo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    fun `test Transpose 04`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    fun `test Transpose 05`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    fun `test Transpose 06`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo b<caret>ar")
    }

    fun `test Transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo bar<caret>")
    }

    fun `test Transpose 10`() {
        myFixture.configureByText(FILE, "f<caret>oo + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 11`() {
        myFixture.configureByText(FILE, "fo<caret>o + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 12`() {
        myFixture.configureByText(FILE, "foo<caret> + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 13`() {
        myFixture.configureByText(FILE, "foo <caret>+ bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 14`() {
        myFixture.configureByText(FILE, "foo +<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 15`() {
        myFixture.configureByText(FILE, "foo + <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    fun `test Transpose 16`() {
        myFixture.configureByText(FILE, "foo + b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + b<caret>ar")
    }

    fun `test Transpose 17`() {
        myFixture.configureByText(FILE, "foo + bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + bar<caret>")
    }

    fun `test Transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    fun `test Transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    fun `test Transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    fun `test Transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.b<caret>ar")
    }

    fun `test Transpose 31`() {
        myFixture.configureByText(FILE, "<caret><selection>foo b</selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    fun `test Transpose 32`() {
        myFixture.configureByText(FILE, "<selection>foo b<caret></selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    fun `test Transpose 41`() {
        myFixture.configureByText(FILE, "foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("barFoo<caret>")
    }

    fun `test Transpose 42`() {
        myFixture.configureByText(FILE, "F<caret>ooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    fun `test Transpose 43`() {
        myFixture.configureByText(FILE, "Foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    fun `test Reverse transpose 01`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    fun `test Reverse transpose 02`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o bar")
    }

    fun `test Reverse transpose 03`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret> bar")
    }

    fun `test Reverse transpose 04`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo <caret>bar")
    }

    fun `test Reverse transpose 05`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    fun `test Reverse transpose 06`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    fun `test Reverse transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret> ")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

    fun `test Reverse transpose 08`() {
        myFixture.configureByText(FILE, "foo bar <caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

    fun `test Reverse transpose 10`() {
        myFixture.configureByText(FILE, "foo () b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

    fun `test Reverse transpose 11`() {
        myFixture.configureByText(FILE, "foo () <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo () <caret>bar")
    }

    fun `test Reverse transpose 12`() {
        myFixture.configureByText(FILE, "foo ()<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo ()<caret> bar")
    }

    fun `test Reverse transpose 13`() {
        myFixture.configureByText(FILE, "baz foo (<caret>) bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo baz () bar")
    }

    fun `test Reverse transpose 14`() {
        myFixture.configureByText(FILE, "baz () foo <caret>() bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo () baz () bar")
    }

    fun `test Reverse transpose 15`() {
        myFixture.configureByText(FILE, "fo<caret>o () bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o () bar")
    }

    fun `test Reverse transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o.bar")
    }

    fun `test Reverse transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret>.bar")
    }

    fun `test Reverse transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.<caret>bar")
    }

    fun `test Reverse transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

    fun `test Reverse transpose 31`() {
        myFixture.configureByText(FILE, "fo<selection><caret>o bar</selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    fun `test Reverse transpose 32`() {
        myFixture.configureByText(FILE, "fo<selection>o bar<caret></selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    fun `test Reverse transpose 41`() {
        myFixture.configureByText(FILE, "fooBa<caret>r")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>barFoo")
    }

    fun `test Reverse transpose 42`() {
        myFixture.configureByText(FILE, "BazFoo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>FooBazBar")
    }

    fun `test Reverse transpose 43`() {
        myFixture.configureByText(FILE, "FooB<caret>ar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>BarFoo")
    }

    fun `test Reverse transpose 44`() {
        myFixture.configureByText(FILE, "foo bar<caret> baz")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo baz")
    }
}
