package com.github.strindberg.emacsj.duplicate

import com.github.strindberg.emacsj.EmacsJServiceImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.registerServiceInstance

class CommentTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        ApplicationManager.getApplication().registerServiceInstance(EmacsJServiceImpl::class.java, EmacsJServiceImpl())
    }

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
