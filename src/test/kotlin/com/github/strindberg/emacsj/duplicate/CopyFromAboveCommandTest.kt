package com.github.strindberg.emacsj.duplicate

import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import org.junit.jupiter.api.Test

private const val FILE = "copyfromabovefile.txt"

class CopyFromAboveCommandTest : EmacsJTestCase() {

    @Test
    fun `Whole line is duplicated from position 0`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |el pueblo unido<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `First non-blank line is copied`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |
                |
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |
                |
                |el pueblo unido<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Nothing is duplicated if on first line`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>el pueblo unido
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |<caret>el pueblo unido
            """.trimMargin()
        )
    }

    @Test
    fun `Nothing is duplicated if only blank lines above`() {
        myFixture.configureByText(
            FILE,
            """
                |
                |
                |<caret>el pueblo unido
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |
                |
                |<caret>el pueblo unido
            """.trimMargin()
        )
    }

    @Test
    fun `With active selection no copy above command action is executed`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |<selection> <caret></selection>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |<selection> <caret></selection>
            """.trimMargin()
        )
    }

    @Test
    fun `Line is duplicated from caret position`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |jamas<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |jamaseblo unido<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Universal argument limits number of characters`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |el p<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Short previous line is handled`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |           jamas<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |           jamas<caret>
            """.trimMargin()
        )
    }

    @Test
    fun `Universal argument does not extend beyond line end`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo unido
                |          <caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo unido
                |          unido
            """.trimMargin()
        )
    }

    @Test
    fun `Copy from above works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |el pueblo
                |<caret>
                |unido
                |<caret>
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_COPY_ABOVE_COMMAND)

        myFixture.checkResult(
            """
                |el pueblo
                |el pueblo<caret>
                |unido
                |unido<caret>
            """.trimMargin()
        )
    }
}
