package com.github.strindberg.emacsj.duplicate

import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val FILE = "file.java"

class CommentTest : BasePlatformTestCase() {

    fun `test Java line is commented`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<caret>    System.out.println(arg) 
              |}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """class Zoo { 
              |//    System.out.println(arg) 
              |<caret>}
            """.trimMargin()
        )
    }

    fun `test Java region is commented 1`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<selection>    System.out.println(arg1) 
              |    System.out.println(arg2)
              |</selection><caret>}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """class Zoo { 
              |//    System.out.println(arg1) 
              |//    System.out.println(arg2)
              |<caret>}
            """.trimMargin()
        )
    }

    fun `test Java region is commented 2`() {
        myFixture.configureByText(
            FILE,
            """System.out.<selection>println(arg1) 
              |System.out.println(arg2)
              |</selection><caret>System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """System.out./*println(arg1) 
              |System.out.println(arg2)
              |*/<caret>System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test Java region is commented 3`() {
        myFixture.configureByText(
            FILE,
            """<selection>System.out.println(arg1)
              |System.</selection><caret>out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """/*System.out.println(arg1)
              |System.*/out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test Java region is commented 4`() {
        myFixture.configureByText(
            FILE,
            """System.out.<selection>println(arg1)
              |System.</selection><caret>out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """System.out./*println(arg1)
              |System.*/out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }
}
