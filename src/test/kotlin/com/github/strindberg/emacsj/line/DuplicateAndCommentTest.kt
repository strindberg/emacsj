package com.github.strindberg.emacsj.line

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val ACTION_COMMENT = "com.github.strindberg.emacsj.actions.line.duplicateandcomment"

class DuplicateAndCommentTest : BasePlatformTestCase() {

    fun `test Duplicated line in commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """<foo>
              |bar
              |<caret><baz>content</baz>
              |</foo>""".trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """<foo>
              |bar
              |<!--<baz>content</baz>-->
              |<caret><baz>content</baz>
              |</foo>""".trimMargin()
        )

    }

    fun `test Duplicated region is commented`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """<foo>
              |<selection>bar
              |<baz>content</baz>
              |</selection><caret></foo>""".trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """<foo>
              |<!--bar-->
              |<!--<baz>content</baz>-->
              |bar
              |<baz>content</baz>
              |<caret></foo>""".trimMargin()
        )

    }

    fun `test New line is inserted between original and copy when caret not at line start`() {
        myFixture.configureByText(
            XmlFileType.INSTANCE,
            """<foo>
              |<selection>bar
              |<baz>content</selection><caret></baz>
              |</foo>""".trimMargin()
        )

        myFixture.performEditorAction(ACTION_COMMENT)

        myFixture.checkResult(
            """<foo>
              |<!--bar-->
              |<!--<baz>content-->
              |bar
              |<baz>content<caret></baz>
              |</foo>""".trimMargin()
        )

    }

}
