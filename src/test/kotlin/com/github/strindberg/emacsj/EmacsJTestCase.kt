package com.github.strindberg.emacsj

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.CHAR_UNDEFINED
import com.github.strindberg.emacsj.mark.MarkPlaces
import com.github.strindberg.emacsj.movement.GotoLineHandler
import com.github.strindberg.emacsj.paste.PasteHandler
import com.github.strindberg.emacsj.search.ISearchHandler
import com.github.strindberg.emacsj.search.ReplaceHandler
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.xref.XRefPlaces
import com.github.strindberg.emacsj.zap.ZapHandler
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.intellij.testFramework.junit5.RunInEdt
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

private const val KEY_EVENT_TIME = 1234L

/**
 * Base class for EmacsJ fixture tests.
 *
 * [RunInEdt] is inherited by every subclass, and `writeIntent = true` is load-bearing: without it any test that
 * changes a document fails with "Write-unsafe context!".
 *
 * The interactive features keep their delegates in a companion-object field. All of it outlives a single test,
 * so cleanup has to happen centrally: doing it per class means the next test class added to the project silently
 * inherits what the previous one left.
 */
@Suppress("AbstractClassCanBeConcreteClass")
@RunInEdt(writeIntent = true)
abstract class EmacsJTestCase {

    protected lateinit var myFixture: CodeInsightTestFixture

    protected val project: Project
        get() = myFixture.project

    @BeforeEach
    fun setUpFixture() {
        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val projectFixture = factory
            .createLightFixtureBuilder(LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR, javaClass.simpleName)
            .fixture
        myFixture = factory.createCodeInsightFixture(projectFixture, LightTempDirTestFixtureImpl(true))
        myFixture.setUp()
    }

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

    /** Runs after any @AfterEach a subclass declares, which is what the old `finally { super.tearDown() }` gave. */
    @AfterEach
    fun tearDownFixture() {
        try {
            ISearchHandler.delegate?.hide()
            ReplaceHandler.delegate?.hide()
            UniversalArgumentHandler.delegate?.hide()
            ZapHandler.delegate?.hide()
            GotoLineHandler.delegate?.hide()
            PasteHandler.killRingDelegate?.hide()

            EmacsJService.instance.setRepeating(false)

            // Action history is application-scoped and outlives the test. Handlers that behave differently when
            // repeated (recenter, reposition, append-kill) would otherwise start mid-cycle in the next test. Two
            // pushes are needed to clear both the last and the previous slot; "" matches no action id.
            repeat(2) { EmacsJService.instance.addAction("") }

            // Project-scoped, but the light fixture hands the same project to every test in a class, so the mark
            // ring and the xref history carry over exactly like the application-scoped state above.
            project.service<MarkPlaces>().clear()
            project.service<XRefPlaces>().stack.clear()
        } finally {
            myFixture.tearDown()
        }
    }
}
