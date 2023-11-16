package com.github.strindberg.emacsj.word

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

const val FILE = "file.txt"

private const val ACTION_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewords"
private const val ACTION_REVERSE_TRANSPOSE_WORDS = "com.github.strindberg.emacsj.actions.word.transposewordsreverse"

@RunWith(JUnit4::class)
class TransposeWordTest : BasePlatformTestCase() {

    @Test
    fun `transpose 00`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `transpose 01`() {
        myFixture.configureByText(FILE, " <caret>foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult(" <caret>foo bar")
    }

    @Test
    fun `transpose 02`() {
        myFixture.configureByText(FILE, "<caret> foo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret> foo bar")
    }

    @Test
    fun `transpose 03`() {
        myFixture.configureByText(FILE, "f<caret>oo bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `transpose 04`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `transpose 05`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar foo<caret>")
    }

    @Test
    fun `transpose 06`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo b<caret>ar")
    }

    @Test
    fun `transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo bar<caret>")
    }

    @Test
    fun `transpose 10`() {
        myFixture.configureByText(FILE, "f<caret>oo + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 11`() {
        myFixture.configureByText(FILE, "fo<caret>o + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 12`() {
        myFixture.configureByText(FILE, "foo<caret> + bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 13`() {
        myFixture.configureByText(FILE, "foo <caret>+ bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 14`() {
        myFixture.configureByText(FILE, "foo +<caret> bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 15`() {
        myFixture.configureByText(FILE, "foo + <caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar + foo<caret>")
    }

    @Test
    fun `transpose 16`() {
        myFixture.configureByText(FILE, "foo + b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + b<caret>ar")
    }

    @Test
    fun `transpose 17`() {
        myFixture.configureByText(FILE, "foo + bar<caret>")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo + bar<caret>")
    }

    @Test
    fun `transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("bar.foo<caret>")
    }

    @Test
    fun `transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.b<caret>ar")
    }

    @Test
    fun `transpose 31`() {
        myFixture.configureByText(FILE, "<caret><selection>foo b</selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    @Test
    fun `transpose 32`() {
        myFixture.configureByText(FILE, "<selection>foo b<caret></selection>ar")
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("ar<selection>foo b<caret></selection>")
    }

    @Test
    fun `transpose 41`() {
        myFixture.configureByText(FILE, "foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("barFoo<caret>")
    }

    @Test
    fun `transpose 42`() {
        myFixture.configureByText(FILE, "F<caret>ooBar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    @Test
    fun `transpose 43`() {
        myFixture.configureByText(FILE, "Foo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_TRANSPOSE_WORDS)
        myFixture.checkResult("BarFoo<caret>")
    }

    @Test
    fun `reverse transpose 01`() {
        myFixture.configureByText(FILE, "<caret>foo bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo bar")
    }

    @Test
    fun `reverse transpose 02`() {
        myFixture.configureByText(FILE, "fo<caret>o bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o bar")
    }

    @Test
    fun `reverse transpose 03`() {
        myFixture.configureByText(FILE, "foo<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret> bar")
    }

    @Test
    fun `reverse transpose 04`() {
        myFixture.configureByText(FILE, "foo <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo <caret>bar")
    }

    @Test
    fun `reverse transpose 05`() {
        myFixture.configureByText(FILE, "foo b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `reverse transpose 06`() {
        myFixture.configureByText(FILE, "foo bar<caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo")
    }

    @Test
    fun `reverse transpose 07`() {
        myFixture.configureByText(FILE, "foo bar<caret> ")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

    @Test
    fun `reverse transpose 08`() {
        myFixture.configureByText(FILE, "foo bar <caret>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo ")
    }

    @Test
    fun `reverse transpose 10`() {
        myFixture.configureByText(FILE, "foo () b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar () foo")
    }

    @Test
    fun `reverse transpose 11`() {
        myFixture.configureByText(FILE, "foo () <caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo () <caret>bar")
    }

    @Test
    fun `reverse transpose 12`() {
        myFixture.configureByText(FILE, "foo ()<caret> bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo ()<caret> bar")
    }

    @Test
    fun `reverse transpose 13`() {
        myFixture.configureByText(FILE, "baz foo (<caret>) bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo baz () bar")
    }

    @Test
    fun `reverse transpose 14`() {
        myFixture.configureByText(FILE, "baz () foo <caret>() bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>foo () baz () bar")
    }

    @Test
    fun `reverse transpose 15`() {
        myFixture.configureByText(FILE, "fo<caret>o () bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o () bar")
    }

    @Test
    fun `reverse transpose 20`() {
        myFixture.configureByText(FILE, "fo<caret>o.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("fo<caret>o.bar")
    }

    @Test
    fun `reverse transpose 21`() {
        myFixture.configureByText(FILE, "foo<caret>.bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo<caret>.bar")
    }

    @Test
    fun `reverse transpose 22`() {
        myFixture.configureByText(FILE, "foo.<caret>bar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("foo.<caret>bar")
    }

    @Test
    fun `reverse transpose 23`() {
        myFixture.configureByText(FILE, "foo.b<caret>ar")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar.foo")
    }

    @Test
    fun `reverse transpose 31`() {
        myFixture.configureByText(FILE, "fo<selection><caret>o bar</selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    @Test
    fun `reverse transpose 32`() {
        myFixture.configureByText(FILE, "fo<selection>o bar<caret></selection>")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<selection><caret>o bar</selection>fo")
    }

    @Test
    fun `reverse transpose 41`() {
        myFixture.configureByText(FILE, "fooBa<caret>r")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>barFoo")
    }

    @Test
    fun `reverse transpose 42`() {
        myFixture.configureByText(FILE, "BazFoo<caret>Bar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>FooBazBar")
    }

    @Test
    fun `reverse transpose 43`() {
        myFixture.configureByText(FILE, "FooB<caret>ar")
        myFixture.editor.settings.isCamelWords = true
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>BarFoo")
    }

    @Test
    fun `reverse transpose 44`() {
        myFixture.configureByText(FILE, "foo bar<caret> baz")
        myFixture.performEditorAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        myFixture.checkResult("<caret>bar foo baz")
    }
}
