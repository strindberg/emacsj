package com.github.strindberg.emacsj

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import com.github.strindberg.emacsj.movement.GotoLineHandler
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase

private const val KEY_EVENT_TIME = 1234L

/**
 * Base class for EmacsJ fixture tests.
 *
 * The interactive features keep their delegates in a companion-object field. All of it outlives a single test,
 * so cleanup has to happen centrally: doing it per class means the next test class added to the project silently
 * inherits what the previous one left.
 */
@Suppress("AbstractClassCanBeConcreteClass")
abstract class EmacsJTestCase : BasePlatformTestCase() {

    /**
     * Sends a key press and release to an interactive command's popup. While such a command is active the popup,
     * not the editor, is what receives keystrokes, so tests have to drive it directly.
     */
    internal fun pressKey(ui: CommonUI?, keyCode: Int) {
        if (ui != null) {
            ui.popup.dispatchKeyEvent(KeyEvent(ui.textField, KeyEvent.KEY_PRESSED, KEY_EVENT_TIME, 0, keyCode, CHAR_UNDEFINED))
            ui.popup.dispatchKeyEvent(KeyEvent(ui.textField, KeyEvent.KEY_RELEASED, KEY_EVENT_TIME, 0, keyCode, CHAR_UNDEFINED))
        }
    }

    override fun tearDown() {
        try {
            ISearchHandler.delegate?.hide()
            ReplaceHandler.delegate?.hide()
            UniversalArgumentHandler.delegate?.hide()
            ZapHandler.delegate?.hide()
            GotoLineHandler.delegate?.hide()

            EmacsJService.instance.setRepeating(false)

            // Action history is application-scoped and outlives the test. Handlers that behave differently when
            // repeated (recenter, reposition, append-kill) would otherwise start mid-cycle in the next test. Two
            // pushes are needed to clear both the last and the previous slot; "" matches no action id.
            repeat(2) { EmacsJService.instance.addAction("") }
        } finally {
            super.tearDown()
        }
    }
}
