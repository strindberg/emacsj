package com.github.strindberg.emacsj.duplicate

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val FILE = "file.java"

class DuplicateTest : BasePlatformTestCase() {

    fun `test Line is duplicated`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<caret>    System.out.println(arg) 
              |}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """class Zoo { 
              |<caret>    System.out.println(arg) 
              |    System.out.println(arg) 
              |}
            """.trimMargin()
        )
    }

    fun `test Line is duplicated at end of buffer`() {
        myFixture.configureByText(
            FILE,
            """System.out.println(arg1)
              |System.out.println(arg2)<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """System.out.println(arg1)
              |System.out.println(arg2)<caret>
              |System.out.println(arg2)
              |
            """.trimMargin()
        )
    }

    fun `test Region is duplicated`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<selection>    System.out.println(arg1) 
              |    System.out.println(arg2)
              |</selection><caret>}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """class Zoo { 
              |    System.out.println(arg1) 
              |    System.out.println(arg2)
              |<caret>    System.out.println(arg1) 
              |    System.out.println(arg2)
              |}
            """.trimMargin()
        )
    }

    fun `test Region is duplicated 2`() {
        myFixture.configureByText(
            FILE,
            """System.out.<selection>println(arg1)
              |System.</selection><caret>out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE)

        myFixture.checkResult(
            """System.out.println(arg1)
              |System.println(arg1)
              |System.out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test Java line is duplicated and commented`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<caret>    System.out.println(arg) 
              |}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """class Zoo { 
              |//    System.out.println(arg) 
              |<caret>    System.out.println(arg) 
              |}
            """.trimMargin()
        )
    }

    fun `test Java region is duplicated and commented 1`() {
        myFixture.configureByText(
            FILE,
            """class Zoo { 
              |<selection>    System.out.println(arg1) 
              |    System.out.println(arg2)
              |</selection><caret>}
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """class Zoo { 
              |//    System.out.println(arg1) 
              |//    System.out.println(arg2)
              |<caret>    System.out.println(arg1) 
              |    System.out.println(arg2)
              |}
            """.trimMargin()
        )
    }

    fun `test Java region is duplicated and commented 2`() {
        myFixture.configureByText(
            FILE,
            """System.out.<selection>println(arg1) 
              |System.out.println(arg2)
              |</selection><caret>System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """System.out./*println(arg1) 
              |System.out.println(arg2)
              |*/<caret>println(arg1) 
              |System.out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test Java region is duplicated and commented 3`() {
        myFixture.configureByText(
            FILE,
            """<selection>System.out.println(arg1)
              |System.</selection><caret>out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """/*System.out.println(arg1)
              |System.*/System.out.println(arg1)
              |System.out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test Java region is duplicated and commented 4`() {
        myFixture.configureByText(
            FILE,
            """System.out.<selection>println(arg1)
              |System.</selection><caret>out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """System.out./*println(arg1)
              |System.*/println(arg1)
              |System.out.println(arg2)
              |System.out.println(arg3)
            """.trimMargin()
        )
    }

    fun `test XML line is duplicated and commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """<foo>
              |bar
              |<caret><baz>content</baz>
              |</foo>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """<foo>
              |bar
              |<!--<baz>content</baz>-->
              |<caret><baz>content</baz>
              |</foo>
            """.trimMargin()
        )
    }

    fun `test XML region is duplicated and commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """<foo>
              |<selection>bar
              |<baz>content</baz>
              |</selection><caret></foo>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_DUPLICATE_COMMENT)

        myFixture.checkResult(
            """<foo>
              |<!--bar-->
              |<!--<baz>content</baz>-->
              |<caret>bar
              |<baz>content</baz>
              |</foo>
            """.trimMargin()
        )
    }
}
