package com.github.strindberg.emacsj.xref

import com.intellij.openapi.actionSystem.IdeActions.ACTION_GOTO_DECLARATION
import com.intellij.openapi.actionSystem.IdeActions.ACTION_GOTO_TYPE_DECLARATION
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "MyClass.kt"

class XRefTest : BasePlatformTestCase() {

    fun `test XRef back works after Go to declaration`() {
        myFixture.configureByText(
            FILE,
            """
                class MyClass {
                    fun main() {
                        he<caret>llo()
                    }
                    fun hello() {
                        println("Hello world!")
                    }
                }
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_GOTO_DECLARATION)
        myFixture.checkResult(
            """
                class MyClass {
                    fun main() {
                        hello()
                    }
                    fun <caret>hello() {
                        println("Hello world!")
                    }
                }
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_XREF_BACK)
        myFixture.checkResult(
            """
                class MyClass {
                    fun main() {
                        he<caret>llo()
                    }
                    fun hello() {
                        println("Hello world!")
                    }
                }
            """.trimIndent()
        )
    }

    fun `test XRef back works after Go to type declaration`() {
        myFixture.configureByText(
            FILE,
            """
                class AClass {}
                class MyClass {
                    fun hello() {
                        val a = A<caret>Class()
                    }
                }
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_GOTO_TYPE_DECLARATION)
        myFixture.checkResult(
            """
                class <caret>AClass {}
                class MyClass {
                    fun hello() {
                        val a = AClass()
                    }
                }
            """.trimIndent()
        )

        myFixture.performEditorAction(ACTION_XREF_BACK)
        myFixture.checkResult(
            """
                class AClass {}
                class MyClass {
                    fun hello() {
                        val a = A<caret>Class()
                    }
                }
            """.trimIndent()
        )
    }
}
