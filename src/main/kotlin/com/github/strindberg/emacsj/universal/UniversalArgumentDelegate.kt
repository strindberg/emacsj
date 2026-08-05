package com.github.strindberg.emacsj.universal

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.duplicate.ACTION_COPY_ABOVE_COMMAND
import com.github.strindberg.emacsj.line.ACTION_TRANSPOSE_LINES
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.github.strindberg.emacsj.paste.ACTION_PREFIX_PASTE
import com.github.strindberg.emacsj.search.ACTION_ISEARCH_BACKWARD
import com.github.strindberg.emacsj.search.ACTION_ISEARCH_FORWARD
import com.github.strindberg.emacsj.search.ACTION_ISEARCH_REGEXP_BACKWARD
import com.github.strindberg.emacsj.search.ACTION_ISEARCH_REGEXP_FORWARD
import com.github.strindberg.emacsj.search.ACTION_REPLACE_REGEXP
import com.github.strindberg.emacsj.search.ACTION_REPLACE_TEXT
import com.github.strindberg.emacsj.space.ACTION_DELETE_SPACE
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.ui.EmacsJTypedActionService
import com.github.strindberg.emacsj.ui.UIDelegate
import com.github.strindberg.emacsj.ui.constructInput
import com.github.strindberg.emacsj.ui.isActive
import com.github.strindberg.emacsj.word.ACTION_TRANSPOSE_WORDS
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_UP_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_UP_TO
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.VisibleForTesting

internal val singleActions = setOf(
    ACTION_ISEARCH_BACKWARD,
    ACTION_ISEARCH_FORWARD,
    ACTION_ISEARCH_REGEXP_FORWARD,
    ACTION_ISEARCH_REGEXP_BACKWARD,
    ACTION_REPLACE_TEXT,
    ACTION_REPLACE_REGEXP,
    ACTION_PASTE,
    ACTION_PREFIX_PASTE,
    ACTION_PUSH_MARK,
    ACTION_POP_MARK,
    ACTION_ZAP_FORWARD_TO,
    ACTION_ZAP_FORWARD_UP_TO,
    ACTION_ZAP_BACKWARD_TO,
    ACTION_ZAP_BACKWARD_UP_TO,
    ACTION_DELETE_SPACE,
    ACTION_TRANSPOSE_LINES,
    ACTION_TRANSPOSE_WORDS,
    ACTION_COPY_ABOVE_COMMAND,
)

const val BATCH_SIZE = 100

class UniversalArgumentDelegate(override val editor: Editor, private var numeric: Int?, val caret: Caret?, val dataContext: DataContext) :
    UIDelegate {

    private var isDisposed = false

    private var counter = 4

    @VisibleForTesting
    internal val ui = CommonUI(editor = editor, isWriteable = false, cancelCallback = ::hide).apply {
        title = "Argument: "
        text = getTimes().toString()
    }

    init {
        EditorUtil.disposeWithEditor(editor, this)

        // Handle dead keys, i.e. accents waiting for their main character. If this dispatcher is not used,
        // typed accents show in the editor until the full character is composed.
        IdeEventQueue.getInstance().addDispatcher(
            { e ->
                val delegate = UniversalArgumentHandler.delegate
                if (delegate.isActive(e)) {
                    e.constructInput()?.let { delegate.handleChar(EmacsJTypedActionService.instance.originalHandler, it.first()) }
                    e.consume()
                    true
                } else {
                    false
                }
            },
            this
        )

        ui.show()
    }

    internal fun handleChar(originalHandler: TypedActionHandler, charTyped: Char) {
        if (charTyped.isDigit()) {
            addDigit(charTyped.digitToInt())
        } else {
            repeatCommand(getTimes()) { originalHandler.execute(editor, charTyped, dataContext) }
        }
    }

    internal fun repeatAction(actionId: String) {
        val times = if (actionId in EmacsJService.instance.getSingleActions()) 1 else getTimes()
        if (times > 1) {
            // The command has already been run once when we get here. We hence subtract 1 from the number of repetitions.
            EditorActionManager.getInstance().getActionHandler(actionId)?.let { handler ->
                repeatCommand(times - 1) { handler.execute(editor, caret, dataContext) }
            }
        }
    }

    internal fun multiply() {
        counter *= 4
        ui.text = getTimes().toString()
    }

    internal fun addDigit(digit: Int) {
        numeric = numeric?.let { 10 * it + digit } ?: digit
        ui.text = getTimes().toString()
    }

    internal fun getTimes(): Int = numeric ?: counter

    internal fun hide() {
        Disposer.dispose(this)
    }

    override fun dispose() {
        if (!isDisposed) { // ui.cancelUI() below re-enters through the popup's cancel callback.
            isDisposed = true

            ui.cancelUI()

            UniversalArgumentHandler.delegate = null
        }
    }

    private fun repeatCommand(times: Int, action: () -> Unit) {
        hide()

        EmacsJService.instance.setRepeating(true)
        repeat(times / BATCH_SIZE) {
            doRepeat(BATCH_SIZE, action)
        }
        doRepeat(times % BATCH_SIZE, action)
        ApplicationManager.getApplication().invokeLater { EmacsJService.instance.setRepeating(false) }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun doRepeat(times: Int, action: () -> Unit) {
        if (times > 0) {
            ApplicationManager.getApplication().invokeLater {
                repeat(times) {
                    if (EmacsJService.instance.isRepeating()) {
                        try {
                            action()
                        } catch (e: Exception) {
                            EmacsJService.instance.setRepeating(false)
                            thisLogger().warn(e)
                        }
                    }
                }
            }
        }
    }
}
