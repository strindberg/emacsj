package com.github.strindberg.emacsj.universal

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.duplicate.ACTION_COPY_ABOVE_COMMAND
import com.github.strindberg.emacsj.line.ACTION_TRANSPOSE_LINES
import com.github.strindberg.emacsj.mark.ACTION_POP_MARK
import com.github.strindberg.emacsj.mark.ACTION_PUSH_MARK
import com.github.strindberg.emacsj.paste.ACTION_HISTORY_PASTE
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
import com.github.strindberg.emacsj.word.ACTION_TRANSPOSE_WORDS
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_UP_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_UP_TO
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import org.jetbrains.annotations.VisibleForTesting

internal val singleActions = [
    ACTION_ISEARCH_BACKWARD,
    ACTION_ISEARCH_FORWARD,
    ACTION_ISEARCH_REGEXP_FORWARD,
    ACTION_ISEARCH_REGEXP_BACKWARD,
    ACTION_REPLACE_TEXT,
    ACTION_REPLACE_REGEXP,
    ACTION_PASTE,
    ACTION_PREFIX_PASTE,
    ACTION_HISTORY_PASTE,
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
]

internal class UniversalArgumentDelegate(
    editor: Editor,
    private var numeric: Int?,
    private val caret: Caret?,
    private val dataContext: DataContext,
) : UIDelegate(editor) {

    private var counter = 4

    @VisibleForTesting
    override val ui = CommonUI(editor = editor, isWriteable = false, cancelCallback = ::hide).apply {
        title = "Argument: "
        text = getTimes().toString() // counter must have been initialized here, do not change the order
    }

    init {
        captureComposedInput { input -> handleChar(EmacsJTypedActionService.instance.originalHandler, input.first()) }

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

    override fun clearDelegate() {
        UniversalArgumentHandler.delegate = null
    }

    private fun repeatCommand(times: Int, action: () -> Unit) {
        hide()
        UniversalArgumentHandler.startRepeat(editor.project, times, action)
    }
}
