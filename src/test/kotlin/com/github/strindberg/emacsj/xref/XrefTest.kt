package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_XREF_GO_FORWARD = "com.github.strindberg.emacsj.actions.xref.goforward"
private const val ACTION_XREF_GO_BACK = "com.github.strindberg.emacsj.actions.xref.goback"

class XrefTest : BasePlatformTestCase() {

    fun `test xef Go Forward`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText("test.java", "class Test {\n public static void foo() {}\n public static void bar() { f<caret>oo() }\n}")
        myFixture.performEditorAction(ACTION_XREF_GO_FORWARD)
        myFixture.checkResult("class Test {\n public static void <caret>foo() {}\n public static void bar() { foo() }\n}")
    }

    fun `test xref Go Back`() {
        MarkHandler.editorTypeId = ""
        myFixture.configureByText("test.java", "class Test {\n public static void foo() {}\n public static void bar() { f<caret>oo() }\n}")
        myFixture.performEditorAction(ACTION_XREF_GO_FORWARD)
        myFixture.checkResult("class Test {\n public static void <caret>foo() {}\n public static void bar() { foo() }\n}")
        myFixture.performEditorAction(ACTION_XREF_GO_BACK)
        myFixture.checkResult("class Test {\n public static void foo() {}\n public static void bar() { f<caret>oo() }\n}")
    }
}
