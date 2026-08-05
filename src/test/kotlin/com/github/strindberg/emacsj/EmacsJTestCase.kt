package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.kill.CopyRegionHandler
import com.github.strindberg.emacsj.movement.GotoLineHandler
import com.github.strindberg.emacsj.search.CommonHighlighter
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.universal.UniversalArgumentDelegate
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Base class for EmacsJ fixture tests.
 *
 * The interactive features keep their delegate in a companion-object field, and several classes flip a global
 * `isTesting` switch. Both outlive a single test, so cleanup has to happen centrally: doing it per class means the
 * next test class added to the project silently inherits whatever the previous one left behind.
 */
@Suppress("AbstractClassCanBeConcreteClass")
abstract class EmacsJTestCase : BasePlatformTestCase() {

    override fun tearDown() {
        try {
            ISearchHandler.delegate?.hide()
            ReplaceHandler.delegate?.hide()
            UniversalArgumentHandler.delegate?.hide()
            ZapHandler.delegate?.hide()
            GotoLineHandler.delegate?.hide()

            EmacsJService.instance.setRepeating(false)

            // Command history is application-scoped and outlives the test. Handlers that behave differently when
            // repeated (recenter, reposition, append-kill) would otherwise start mid-cycle in the next test. Two
            // pushes are needed to clear both the last and the previous slot; "" matches no command name.
            repeat(2) { EmacsJService.instance.addCommand("") }

            CommonHighlighter.isTesting = false
            UniversalArgumentDelegate.isTesting = false
            CopyRegionHandler.isTesting = false
        } finally {
            super.tearDown() // Must run even if cleanup above throws, or the fixture itself leaks.
        }
    }
}
