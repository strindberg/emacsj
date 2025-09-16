package com.github.strindberg.emacsj.xref

import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_GOTO_DECLARATION
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val FILE = "MyClass.kt"

class XRefTest : BasePlatformTestCase() {

    fun `test XRef redo after back works`() {
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
        repeat(4) { myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT) }

        myFixture.checkResult(
            """
            class MyClass {
                fun main() {
                    hello()
                }
                fun hell<caret>o() {
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

        // Redo forward
        myFixture.performEditorAction(ACTION_XREF_FORWARD)

        myFixture.checkResult(
            """
            class MyClass {
                fun main() {
                    hello()
                }
                fun hell<caret>o() {
                    println("Hello world!")
                }
            }
            """.trimIndent()
        )
    }

    fun `test XRef push clears redo stack`() {
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

        // Go to definition
        myFixture.performEditorAction(ACTION_GOTO_DECLARATION)
        myFixture.performEditorAction(ACTION_XREF_BACK)

        // Push new location → should clear redo
        myFixture.performEditorAction(ACTION_XREF_PUSH)

        // Move caret to the start
        myFixture.editor.caretModel.moveToOffset(0)

        // Redo should do nothing now
        val beforeOffset = myFixture.editor.caretModel.offset
        myFixture.performEditorAction(ACTION_XREF_FORWARD)
        val afterOffset = myFixture.editor.caretModel.offset

        assertEquals(beforeOffset, afterOffset)
    }

    fun `test XRef multiple jump chain`() {
        myFixture.configureByText(
            FILE,
            """
        class AClass {}
        class BClass {}
        class CClass {}
        class Main {
            fun f() {
                val a = A<caret>Class()
                val b = BClass()
                val c = CClass()
            }
        }
            """.trimIndent()
        )

        // Push #1: at AClass reference
        myFixture.performEditorAction(ACTION_XREF_PUSH)

        // Move to BClass() and push
        val bOffset = myFixture.file.text.indexOf("BClass")
        myFixture.editor.caretModel.moveToOffset(bOffset + 1) // place inside "BClass"
        myFixture.performEditorAction(ACTION_XREF_PUSH)

        // Move to CClass() and push
        val cOffset = myFixture.file.text.indexOf("CClass")
        myFixture.editor.caretModel.moveToOffset(cOffset + 1)
        myFixture.performEditorAction(ACTION_XREF_PUSH)

        // Go back step by step
        myFixture.performEditorAction(ACTION_XREF_BACK) // to BClass
        val pos1 = myFixture.editor.caretModel.offset
        myFixture.performEditorAction(ACTION_XREF_BACK) // to AClass
        val pos2 = myFixture.editor.caretModel.offset

        // Go forward twice
        myFixture.performEditorAction(ACTION_XREF_FORWARD)
        val pos3 = myFixture.editor.caretModel.offset
        myFixture.performEditorAction(ACTION_XREF_FORWARD)
        val pos4 = myFixture.editor.caretModel.offset

        // Should be back at CClass
        val expectedOffset = cOffset + 1
        assertEquals(expectedOffset, pos4)

        // Assert movement occurred and matches expectations
        assertTrue(pos1 > pos2)
        assertTrue(pos3 > pos2)
        assertEquals(expectedOffset, pos4)
    }

    fun `test XRef forward and back are safe at boundaries`() {
        myFixture.configureByText(
            FILE,
            """
            class MyClass {
                fun hello() {
                    println("He<caret>llo world!")
                }
            }
            """.trimIndent()
        )

        val startOffset = myFixture.editor.caretModel.offset

        myFixture.performEditorAction(ACTION_XREF_BACK)
        assertEquals(startOffset, myFixture.editor.caretModel.offset)

        myFixture.performEditorAction(ACTION_XREF_FORWARD)
        assertEquals(startOffset, myFixture.editor.caretModel.offset)
    }
}
