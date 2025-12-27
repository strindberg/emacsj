package com.github.strindberg.emacsj.duplicate

import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val FILE = "file.txt"

class CopyFromAboveCommandTest : BasePlatformTestCase() {

    fun `test Whole line is duplicated from position 0`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |el pueblo unido<caret>
            """.trimMargin()
        )
    }

    fun `test First non-blank line is copied`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |
              |
              |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |
              |
              |el pueblo unido<caret>
            """.trimMargin()
        )
    }

    fun `test Nothing is duplicated if on first line`() {
        myFixture.configureByText(
            FILE,
            """<caret>el pueblo unido
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """<caret>el pueblo unido
            """.trimMargin()
        )
    }

    fun `test Nothing is duplicated if only blank lines above`() {
        myFixture.configureByText(
            FILE,
            """|
               |
               |<caret>el pueblo unido
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """|
               |
               |<caret>el pueblo unido
            """.trimMargin()
        )
    }

    fun `test With active selection no copy above command action is executed`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |<selection> <caret></selection>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |<selection> <caret></selection>
            """.trimMargin()
        )
    }

    fun `test Line is duplicated from caret position`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |jamas<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |jamaseblo unido<caret>
            """.trimMargin()
        )
    }

    fun `test Universal argument limits number of characters`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |el p<caret>
            """.trimMargin()
        )
    }

    fun `test Short previous line is handled`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |           jamas<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |           jamas<caret>
            """.trimMargin()
        )
    }

    fun `test Universal argument does not extend beyond line end`() {
        myFixture.configureByText(
            FILE,
            """el pueblo unido
              |          <caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """el pueblo unido
              |          unido
            """.trimMargin()
        )
    }
}
