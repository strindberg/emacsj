package com.github.strindberg.emacsj.ui

import java.awt.AWTEvent
import java.awt.Component
import java.awt.event.InputMethodEvent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.UIUtil

/**
 * A multi-keystroke command driven from a popup: incremental search, replace, zap, universal argument, goto line.
 *
 * Teardown is shared rather than repeated per feature, because three separate things have to be true at once and
 * getting any of them wrong is silent:
 *
 * - it must run when the editor or project closes mid-command, which is why every subclass parents itself to the
 *   editor with `EditorUtil.disposeWithEditor` and why [hide] goes through [Disposer] rather than calling
 *   [dispose] directly;
 * - it must be idempotent, because canceling the popup re-enters here through the popup's cancel callback;
 * - [release] must run *before* the popup is canceled, since some subclasses still read the popup's text.
 */
internal abstract class UIDelegate(val editor: Editor) : Disposable {

    private var isDisposed = false

    abstract val ui: PopupUI

    protected open val isCancelInhibited: Boolean
        get() = false

    protected open fun release() {}

    protected abstract fun clearDelegate()

    internal fun hide() {
        if (!isCancelInhibited) {
            Disposer.dispose(this)
        }
    }

    final override fun dispose() {
        if (!isDisposed) {
            isDisposed = true

            release()
            ui.cancelUI()
            clearDelegate()
        }
    }
}

@OptIn(ExperimentalContracts::class)
internal fun UIDelegate?.isActive(e: AWTEvent): Boolean {
    contract {
        returns(true) implies (this@isActive != null && e is InputMethodEvent)
    }
    return this != null && e is InputMethodEvent && UIUtil.isDescendingFrom(e.source as? Component, editor.contentComponent)
}

internal fun InputMethodEvent.constructInput(): String? =
    text?.let { iter ->
        buildString {
            var c = iter.first()
            repeat(committedCharacterCount) {
                append(c)
                c = iter.next()
            }
        }.takeIf { it.isNotEmpty() }
    }
