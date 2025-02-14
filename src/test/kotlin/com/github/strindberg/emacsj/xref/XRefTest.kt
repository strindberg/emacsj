package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.EmacsJCommandListener
import com.intellij.openapi.actionSystem.IdeActions.ACTION_GOTO_DECLARATION
import com.intellij.testFramework.fixtures.BasePlatformTestCase

const val FILE = "MyClass.kt"

private const val ACTION_XREF_BACK = "com.github.strindberg.emacsj.actions.xref.xrefback"

class XRefTest : BasePlatformTestCase() {

    fun `test XRef back works`() {
        EmacsJCommandListener.editorTypeId = ""

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
}
