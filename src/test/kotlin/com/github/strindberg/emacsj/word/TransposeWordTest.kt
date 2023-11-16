package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "file.txt"

private const val ACTION_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewords"
private const val ACTION_REVERSE_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewordsreverse"

class TransposeWordTest : BasePlatformTestCase() {

        fun `testTranspose 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

        fun `testTranspose 01`() {
        myFixture.configureByText(FILE, " <caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult(" <caret>foo bar")
    }

        fun `testTranspose 02`() {
        myFixture.configureByText(FILE, "<caret> foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret> foo bar")
    }

        fun `testTranspose 03`() {
        myFixture.configureByText(FILE, "f<caret>oo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

        fun `testTranspose 04`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

        fun `testTranspose 05`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

        fun `testTranspose 06`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo b<caret>ar")
    }

        fun `testTranspose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo bar<caret>")
    }

        fun `testTranspose 10`() {
        myFixture.configureByText(FILE, "f<caret>oo + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 11`() {
        myFixture.configureByText(FILE, "fo<caret>o + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 12`() {
        myFixture.configureByText(FILE, "foo<caret> + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 13`() {
        myFixture.configureByText(FILE, "foo <caret>+ bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 14`() {
        myFixture.configureByText(FILE, "foo +<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 15`() {
        myFixture.configureByText(FILE, "foo + <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

        fun `testTranspose 16`() {
        myFixture.configureByText(FILE, "foo + b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + b<caret>ar")
    }

        fun `testTranspose 17`() {
        myFixture.configureByText(FILE, "foo + bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + bar<caret>")
    }

        fun `testTranspose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

        fun `testTranspose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

        fun `testTranspose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

        fun `testTranspose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.b<caret>ar")
    }

        fun `testTranspose 31`() {
        myFixture.configureByText(FILE, "<caret><selection>foo b</selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

        fun `testTranspose 32`() {
        myFixture.configureByText(FILE, "<selection>foo b<caret></selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

        fun `testTranspose 41`() {
        myFixture.configureByText(FILE, "foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("barFoo<caret>")
    }

        fun `testTranspose 42`() {
        myFixture.configureByText(FILE, "F<caret>ooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

        fun `testTranspose 43`() {
        myFixture.configureByText(FILE, "Foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

        fun `testReverse transpose 01`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

        fun `testReverse transpose 02`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o bar")
    }

        fun `testReverse transpose 03`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret> bar")
    }

        fun `testReverse transpose 04`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo <caret>bar")
    }

        fun `testReverse transpose 05`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

        fun `testReverse transpose 06`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

        fun `testReverse transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret> ")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

        fun `testReverse transpose 08`() {
        myFixture.configureByText(FILE, "foo bar <caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

        fun `testReverse transpose 10`() {
        myFixture.configureByText(FILE, "foo () b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

        fun `testReverse transpose 11`() {
        myFixture.configureByText(FILE, "foo () <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo () <caret>bar")
    }

        fun `testReverse transpose 12`() {
        myFixture.configureByText(FILE, "foo ()<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo ()<caret> bar")
    }

        fun `testReverse transpose 13`() {
        myFixture.configureByText(FILE, "baz foo (<caret>) bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo baz () bar")
    }

        fun `testReverse transpose 14`() {
        myFixture.configureByText(FILE, "baz () foo <caret>() bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo () baz () bar")
    }

        fun `testReverse transpose 15`() {
        myFixture.configureByText(FILE, "fo<caret>o () bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o () bar")
    }

        fun `testReverse transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o.bar")
    }

        fun `testReverse transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret>.bar")
    }

        fun `testReverse transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.<caret>bar")
    }

        fun `testReverse transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

        fun `testReverse transpose 31`() {
        myFixture.configureByText(FILE, "fo<selection><caret>o bar</selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

        fun `testReverse transpose 32`() {
        myFixture.configureByText(FILE, "fo<selection>o bar<caret></selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

        fun `testReverse transpose 41`() {
        myFixture.configureByText(FILE, "fooBa<caret>r")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>barFoo")
    }

        fun `testReverse transpose 42`() {
        myFixture.configureByText(FILE, "BazFoo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>FooBazBar")
    }

        fun `testReverse transpose 43`() {
        myFixture.configureByText(FILE, "FooB<caret>ar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>BarFoo")
    }

        fun `testReverse transpose 44`() {
        myFixture.configureByText(FILE, "foo bar<caret> baz")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo baz")
    }
}
