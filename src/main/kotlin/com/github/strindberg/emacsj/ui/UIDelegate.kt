package com.github.strindberg.emacsj.ui

import java.awt.Component
import java.awt.event.InputMethodEvent
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.Disposer
import com.intellij.util.ui.UIUtil

/** A multi-keystroke command driven from a popup: incremental search, replace, zap, universal argument, goto line. */
internal abstract class UIDelegate(val editor: Editor) : Disposable {

    private var isDisposed = false

    abstract val ui: PopupUI

    protected open val isCancelInhibited: Boolean
        get() = false

    protected open fun release() {}

    protected abstract fun clearDelegate()

    init {
        EditorUtil.disposeWithEditor(editor, this)
    }

    internal fun hide() {
        if (!isCancelInhibited) {
            Disposer.dispose(this)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    final override fun dispose() {
        if (!isDisposed) {
            isDisposed = true

            try {
                release()
                ui.cancelUI()
            } catch (e: Exception) {
                thisLogger().error(e)
            }

            clearDelegate()
        }
    }

    /**
     * Routes composed input -- a dead key's accent together with the character it was waiting for -- to [handle]
     * rather than to the editor, for as long as this delegate is up. Without it the bare accent lands in the
     * document while the composition is still unfinished.
     */
    protected fun captureComposedInput(handle: (String) -> Unit) {
        IdeEventQueue.getInstance().addDispatcher(
            { event ->
                val composed = (event as? InputMethodEvent)?.takeIf { isAimedAtEditor(it) }
                if (composed == null) {
                    false
                } else {
                    // Consumed even when nothing was committed yet: that is the half-finished accent being kept
                    // out of the document.
                    composed.committedText()?.let(handle)
                    composed.consume()
                    true
                }
            },
            this
        )
    }

    private fun isAimedAtEditor(event: InputMethodEvent): Boolean =
        UIUtil.isDescendingFrom(event.source as? Component, editor.contentComponent)
}

/** The characters the input method has finished composing, if it has committed any yet. */
private fun InputMethodEvent.committedText(): String? =
    text?.let { iter ->
        buildString {
            var c = iter.first()
            repeat(committedCharacterCount) {
                append(c)
                c = iter.next()
            }
        }.takeIf { it.isNotEmpty() }
    }
