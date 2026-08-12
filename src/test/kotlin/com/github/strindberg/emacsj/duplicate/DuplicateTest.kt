package com.github.strindberg.emacsj.duplicate

import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.ide.highlighter.XmlFileType
import org.junit.jupiter.api.Test

private const val FILE = "duplicatefile.java"

class DuplicateTest : EmacsJTestCase() {

    @Test
    fun `Line is duplicated`() {
        myFixture.configureByText(
            FILE,
            """
                |class Zoo { 
                |<caret>    System.out.println(arg) 
                |}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """
                |class Zoo { 
                |<caret>    System.out.println(arg) 
                |    System.out.println(arg) 
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `Line is duplicated at end of buffer`() {
        myFixture.configureByText(
            FILE,
            """
                |System.out.println(arg1)
                |System.out.println(arg2)<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """
                |System.out.println(arg1)
                |System.out.println(arg2)<caret>
                |System.out.println(arg2)
                |
            """.trimMargin()
        )
    }

    @Test
    fun `Region is duplicated`() {
        myFixture.configureByText(
            FILE,
            """
                |class Zoo { 
                |<selection>    System.out.println(arg1) 
                |    System.out.println(arg2)
                |</selection><caret>}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """
                |class Zoo { 
                |    System.out.println(arg1) 
                |    System.out.println(arg2)
                |<caret>    System.out.println(arg1) 
                |    System.out.println(arg2)
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `Region is duplicated 2`() {
        myFixture.configureByText(
            FILE,
            """
                |System.out.<selection>println(arg1)
                |System.</selection><caret>out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """
                |System.out.println(arg1)
                |System.println(arg1)
                |System.out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )
    }

    @Test
    fun `Java line is duplicated and commented`() {
        myFixture.configureByText(
            FILE,
            """
                |class Zoo { 
                |<caret>    System.out.println(arg) 
                |}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |class Zoo { 
                |//    System.out.println(arg) 
                |<caret>    System.out.println(arg) 
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `Java region is duplicated and commented 1`() {
        myFixture.configureByText(
            FILE,
            """
                |class Zoo { 
                |<selection>    System.out.println(arg1) 
                |    System.out.println(arg2)
                |</selection><caret>}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |class Zoo { 
                |//    System.out.println(arg1) 
                |//    System.out.println(arg2)
                |<caret>    System.out.println(arg1) 
                |    System.out.println(arg2)
                |}
            """.trimMargin()
        )
    }

    @Test
    fun `Java region is duplicated and commented 2`() {
        myFixture.configureByText(
            FILE,
            """
                |System.out.<selection>println(arg1) 
                |System.out.println(arg2)
                |</selection><caret>System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |System.out./*println(arg1) 
                |System.out.println(arg2)
                |*/<caret>println(arg1) 
                |System.out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )
    }

    @Test
    fun `Java region is duplicated and commented 3`() {
        myFixture.configureByText(
            FILE,
            """
                |<selection>System.out.println(arg1)
                |System.</selection><caret>out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |/*System.out.println(arg1)
                |System.*/System.out.println(arg1)
                |System.out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )
    }

    @Test
    fun `Java region is duplicated and commented 4`() {
        myFixture.configureByText(
            FILE,
            """
                |System.out.<selection>println(arg1)
                |System.</selection><caret>out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |System.out./*println(arg1)
                |System.*/println(arg1)
                |System.out.println(arg2)
                |System.out.println(arg3)
            """.trimMargin()
        )
    }

    @Test
    fun `XML line is duplicated and commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """
                |<foo>
                |bar
                |<caret><baz>content</baz>
                |</foo>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |<foo>
                |bar
                |<!--<baz>content</baz>-->
                |<caret><baz>content</baz>
                |</foo>
            """.trimMargin()
        )
    }

    @Test
    fun `XML region is duplicated and commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """
                |<foo>
                |<selection>bar
                |<baz>content</baz>
                |</selection><caret></foo>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """
                |<foo>
                |<!--bar-->
                |<!--<baz>content</baz>-->
                |<caret>bar
                |<baz>content</baz>
                |</foo>
            """.trimMargin()
        )
    }
}
