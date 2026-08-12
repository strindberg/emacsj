package com.github.strindberg.emacsj.zap

import java.awt.event.KeyEvent.VK_ESCAPE
import com.github.strindberg.emacsj.EmacsJTestCase
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT3
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT5
import org.junit.jupiter.api.Test

private const val FILE = "zapfile.txt"

class ZapTest : EmacsJTestCase() {

    @Test
    fun `Zap up to works`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("l")

        myFixture.checkResult("bar <caret>l baz")
    }

    @Test
    fun `Zap up to does not remove anything when no match is found`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("q")

        myFixture.checkResult("bar <caret>fool baz")
    }

    @Test
    fun `Zap up to works when matching next char`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret>fool baz")
    }

    @Test
    fun `Zap up to works at buffer end`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_UP_TO)
        myFixture.type("z")

        myFixture.checkResult("bar <caret>z")
    }

    @Test
    fun `Zap to works`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("l")

        myFixture.checkResult("bar <caret> baz")
    }

    @Test
    fun `Zap to does not remove anything when no match is found`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("q")

        myFixture.checkResult("bar <caret>fool baz")
    }

    @Test
    fun `Zap to works when matching next char`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret>ool baz")
    }

    @Test
    fun `Zap to works at buffer end`() {
        myFixture.configureByText(FILE, "bar <caret>fool baz")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("z")

        myFixture.checkResult("bar <caret>")
    }

    @Test
    fun `Zap back up to works`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("f")

        myFixture.checkResult("bar f<caret> baz")
    }

    @Test
    fun `Zap back up to does not remove anything when no match is found`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("q")

        myFixture.checkResult("bar fool<caret> baz")
    }

    @Test
    fun `Zap back up to works when matching previous char`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("l")

        myFixture.checkResult("bar fool<caret> baz")
    }

    @Test
    fun `Zap back up to works at buffer start`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type("b")

        myFixture.checkResult("b<caret> baz")
    }

    @Test
    fun `Zap back to works`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("f")

        myFixture.checkResult("bar <caret> baz")
    }

    @Test
    fun `Zap back to does not remove anything when no match is found`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("q")

        myFixture.checkResult("bar fool<caret> baz")
    }

    @Test
    fun `Zap back to works when matching previous char`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("l")

        myFixture.checkResult("bar foo<caret> baz")
    }

    @Test
    fun `Zap back to works at buffer start`() {
        myFixture.configureByText(FILE, "bar fool<caret> baz")

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("b")

        myFixture.checkResult("<caret> baz")
    }

    @Test
    fun `Zap with capital letter only matches capital letters`() {
        myFixture.configureByText(FILE, "<caret>bar foo Foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("F")

        myFixture.checkResult("<caret>oo")
    }

    @Test
    fun `Zap with lower-case letter matches capital letters`() {
        myFixture.configureByText(FILE, "<caret>bar Foo foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("F")

        myFixture.checkResult("<caret>oo foo")
    }

    @Test
    fun `Zap with universal argument removes 4 occurrences`() {
        myFixture.configureByText(FILE, "<caret>) ) ) ) )")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type(")")

        myFixture.checkResult("<caret> )")
    }

    @Test
    fun `Zap with numeric universal argument removes 5 occurrences`() {
        myFixture.configureByText(FILE, "<caret>) ) ) ) )")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type(")")

        myFixture.checkResult("<caret>")
    }

    @Test
    fun `Zap with universal argument higher than existing occurrences removes no text`() {
        myFixture.configureByText(FILE, "<caret>) ) ) ) )")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type(")")

        myFixture.checkResult("<caret>) ) ) ) )")
    }

    @Test
    fun `Zap up to backwards with universal argument removes 3 occurrences`() {
        myFixture.configureByText(FILE, ") ) ) ) )<caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_UP_TO)
        myFixture.type(")")

        myFixture.checkResult(") )<caret>")
    }

    @Test
    fun `Zap backwards with numeric universal argument removes 3 occurrences`() {
        myFixture.configureByText(FILE, ") ) ) ) )<caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type(")")

        myFixture.checkResult(") ) <caret>")
    }

    @Test
    fun `Zap backwards with numeric universal argument higher than existing occurrences removes no text`() {
        myFixture.configureByText(FILE, ") ) ) ) )<caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type(")")

        myFixture.checkResult(") ) ) ) )<caret>")
    }

    @Test
    fun `Universal argument applies to every caret`() {
        myFixture.configureByText(
            FILE,
            """
                |a<caret>bxcxd e
                |f<caret>gxhxi j
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT2)
        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("x")

        myFixture.checkResult(
            """
                |a<caret>d e
                |f<caret>i j
            """.trimMargin()
        )
    }

    @Test
    fun `Zap forward works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |bar <caret>fool baz
                |qux <caret>fool zed
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        myFixture.type("l")

        myFixture.checkResult(
            """
                |bar <caret> baz
                |qux <caret> zed
            """.trimMargin()
        )
    }

    @Test
    fun `Zap backward works with multiple carets`() {
        myFixture.configureByText(
            FILE,
            """
                |bar fool<caret> baz
                |qux fool<caret> zed
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_ZAP_BACKWARD_TO)
        myFixture.type("f")

        myFixture.checkResult(
            """
                |bar <caret> baz
                |qux <caret> zed
            """.trimMargin()
        )
    }

    @Test
    fun `Pressing escape aborts`() {
        myFixture.configureByText(FILE, "<caret>bar Foo foo")

        myFixture.performEditorAction(ACTION_ZAP_FORWARD_TO)
        pressEscape()

        myFixture.checkResult("<caret>bar Foo foo")
    }

    private fun pressEscape() {
        pressKey(ZapHandler.delegate?.ui, VK_ESCAPE)
    }
}
