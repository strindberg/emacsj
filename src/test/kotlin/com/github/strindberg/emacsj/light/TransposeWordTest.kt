package com.github.strindberg.emacsj.light

import com.github.strindberg.emacsj.EmacsJServiceImpl
import com.github.strindberg.emacsj.word.ACTION_REVERSE_TRANSPOSE_WORDS
import com.github.strindberg.emacsj.word.ACTION_TRANSPOSE_WORDS
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.LightPlatformCodeInsightTestCase
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.registerServiceInstance

private const val FILE = "file.txt"

class TransposeWordTest : LightPlatformCodeInsightTestCase() {

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(EmacsJServiceImpl::class.java, EmacsJServiceImpl())
    }

    fun `test Transpose 00`() {
        configureFromFileText(FILE, "<caret>foo bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar foo<caret>")
    }

    fun `test Transpose 01`() {
        configureFromFileText(FILE, " <caret>foo bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar  foo<caret>")
    }

    fun `test Transpose 02`() {
        configureFromFileText(FILE, "<caret> foo bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar  foo<caret>")
    }

    fun `test Transpose 03`() {
        configureFromFileText(FILE, "f<caret>oo bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar foo<caret>")
    }

    fun `test Transpose 04`() {
        configureFromFileText(FILE, "foo<caret> bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar foo<caret>")
    }

    fun `test Transpose 05`() {
        configureFromFileText(FILE, "foo <caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar foo<caret>")
    }

    fun `test Transpose 06`() {
        configureFromFileText(FILE, "foo b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("foo bar<caret>")
    }

    fun `test Transpose 07`() {
        configureFromFileText(FILE, "foo bar<caret>")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("foo bar<caret>")
    }

    fun `test Transpose 10`() {
        configureFromFileText(FILE, "f<caret>oo + bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 11`() {
        configureFromFileText(FILE, "fo<caret>o + bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 12`() {
        configureFromFileText(FILE, "foo<caret> + bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 13`() {
        configureFromFileText(FILE, "foo <caret>+ bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 14`() {
        configureFromFileText(FILE, "foo +<caret> bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 15`() {
        configureFromFileText(FILE, "foo + <caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar + foo<caret>")
    }

    fun `test Transpose 16`() {
        configureFromFileText(FILE, "foo + b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("foo + bar<caret>")
    }

    fun `test Transpose 17`() {
        configureFromFileText(FILE, "foo + bar<caret>")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("foo + bar<caret>")
    }

    fun `test Transpose 20`() {
        configureFromFileText(FILE, "fo<caret>o.bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar.foo<caret>")
    }

    fun `test Transpose 21`() {
        configureFromFileText(FILE, "foo<caret>.bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar.foo<caret>")
    }

    fun `test Transpose 22`() {
        configureFromFileText(FILE, "foo.<caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("bar.foo<caret>")
    }

    fun `test Transpose 23`() {
        configureFromFileText(FILE, "foo.b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("foo.bar<caret>")
    }

    fun `test Transpose 31`() {
        configureFromFileText(FILE, "<caret><selection>foo b</selection>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("ar<selection>foo b<caret></selection>")
    }

    fun `test Transpose 32`() {
        configureFromFileText(FILE, "<selection>foo b<caret></selection>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("ar<selection>foo b<caret></selection>")
    }

    fun `test Transpose 41`() {
        configureFromFileText(FILE, "foo<caret>Bar")
        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("barFoo<caret>")
    }

    fun `test Transpose 42`() {
        configureFromFileText(FILE, "F<caret>ooBar")
        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("BarFoo<caret>")
    }

    fun `test Transpose 43`() {
        configureFromFileText(FILE, "Foo<caret>Bar")
        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_TRANSPOSE_WORDS)
        checkResultByText("BarFoo<caret>")
    }

    fun `test Reverse transpose 01`() {
        configureFromFileText(FILE, "<caret>foo bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>foo bar")
    }

    fun `test Reverse transpose 02`() {
        configureFromFileText(FILE, "fo<caret>o bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>foo bar")
    }

    fun `test Reverse transpose 03`() {
        configureFromFileText(FILE, "foo<caret> bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar foo")
    }

    fun `test Reverse transpose 04`() {
        configureFromFileText(FILE, "foo <caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar foo")
    }

    fun `test Reverse transpose 05`() {
        configureFromFileText(FILE, "foo b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar foo")
    }

    fun `test Reverse transpose 06`() {
        configureFromFileText(FILE, "foo bar<caret>")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar foo")
    }

    fun `test Reverse transpose 07`() {
        configureFromFileText(FILE, "foo bar<caret> ")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar  foo")
    }

    fun `test Reverse transpose 08`() {
        configureFromFileText(FILE, "foo bar <caret>")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar  foo")
    }

    fun `test Reverse transpose 10`() {
        configureFromFileText(FILE, "foo () b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar () foo")
    }

    fun `test Reverse transpose 11`() {
        configureFromFileText(FILE, "foo () <caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar () foo")
    }

    fun `test Reverse transpose 12`() {
        configureFromFileText(FILE, "foo ()<caret> bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar () foo")
    }

    fun `test Reverse transpose 13`() {
        configureFromFileText(FILE, "baz foo (<caret>) bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("baz <caret>bar () foo")
    }

    fun `test Reverse transpose 14`() {
        configureFromFileText(FILE, "baz () foo <caret>() bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("baz () <caret>bar () foo")
    }

    fun `test Reverse transpose 15`() {
        configureFromFileText(FILE, "fo<caret>o () bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>foo () bar")
    }

    fun `test Reverse transpose 20`() {
        configureFromFileText(FILE, "fo<caret>o.bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>foo.bar")
    }

    fun `test Reverse transpose 21`() {
        configureFromFileText(FILE, "foo<caret>.bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar.foo")
    }

    fun `test Reverse transpose 22`() {
        configureFromFileText(FILE, "foo.<caret>bar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar.foo")
    }

    fun `test Reverse transpose 23`() {
        configureFromFileText(FILE, "foo.b<caret>ar")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>bar.foo")
    }

    fun `test Reverse transpose 31`() {
        configureFromFileText(FILE, "fo<selection><caret>o bar</selection>")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<selection><caret>o bar</selection>fo")
    }

    fun `test Reverse transpose 32`() {
        configureFromFileText(FILE, "fo<selection>o bar<caret></selection>")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<selection><caret>o bar</selection>fo")
    }

    fun `test Reverse transpose 41`() {
        configureFromFileText(FILE, "fooBa<caret>r")

        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>barFoo")
    }

    fun `test Reverse transpose 42`() {
        configureFromFileText(FILE, "BazFoo<caret>Bar")
        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("Baz<caret>BarFoo")
    }

    fun `test Reverse transpose 43`() {
        configureFromFileText(FILE, "FooB<caret>ar")
        editor.settings.isCamelWords = true
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("<caret>BarFoo")
    }

    fun `test Reverse transpose 44`() {
        configureFromFileText(FILE, "foo bar<caret> baz")
        PlatformTestUtil.invokeNamedAction(ACTION_REVERSE_TRANSPOSE_WORDS)
        checkResultByText("foo <caret>baz bar")
    }
}
