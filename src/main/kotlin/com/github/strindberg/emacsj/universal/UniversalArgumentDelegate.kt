package com.github.strindberg.emacsj.universal

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
import com.github.strindberg.emacsj.search.CommonUI
import com.github.strindberg.emacsj.search.RestorableActionHandler
import com.github.strindberg.emacsj.search.RestorableTypedActionHandler
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_BACKWARD_UP_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_TO
import com.github.strindberg.emacsj.zap.ACTION_ZAP_FORWARD_UP_TO
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import org.jetbrains.annotations.VisibleForTesting

class UniversalArgumentDelegate(val editor: Editor, private var numeric: Int?) {

    // Prevent dead keys such as '^' and '~' from showing up in the editor when collecting arguments.
    private val document: Document = this.editor.document.apply { setReadOnly(true) }

    private val typedHandler: RestorableTypedActionHandler

    private val actionHandlers: List<RestorableActionHandler<UniversalArgumentDelegate>>

    private var counter = 4

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::hide).apply {
        title = "Argument: "
        text = getTimes().toString()
    }

    companion object {

        @VisibleForTesting
        internal var testing = false

        private val singleActions = mutableSetOf(
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
        )

        internal fun registerSingleAction(actionId: String) {
            singleActions.add(actionId)
        }
    }

    init {
        TypedAction.getInstance().apply {
            setupRawHandler(
                object : RestorableTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        if (UniversalArgumentHandler.delegate != null) {
                            if (charTyped.isDigit()) {
                                addDigit(charTyped.digitToInt())
                            } else {
                                repeatAction(getTimes()) { myOriginalHandler?.execute(editor, charTyped, dataContext) }
                            }
                        } else {
                            myOriginalHandler?.execute(editor, charTyped, dataContext)
                        }
                    }
                }.also { typedHandler = it }
            )
        }

        EditorActionManager.getInstance().apply {
            actionHandlers = buildList {
                editorActions().forEach { actionId ->
                    getActionHandler(actionId)?.let { originalHandler ->
                        setActionHandler(
                            actionId,
                            RestorableActionHandler(
                                actionId,
                                originalHandler,
                                { UniversalArgumentHandler.delegate }
                            ) { caret, dataContext ->
                                val times = if (actionId in singleActions) 1 else getTimes()
                                repeatAction(times) { originalHandler.execute(editor, caret, dataContext) }
                            }.also { add(it) }
                        )
                    }
                }
            }
        }

        ui.show()
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
        document.setReadOnly(false)

        unregisterHandlers()

        ui.cancelUI()

        UniversalArgumentHandler.delegate = null
    }

    private fun repeatAction(times: Int, action: () -> Unit) {
        document.setReadOnly(false)
        cancel()
        if (times == 1 || testing) {
            repeat(times) {
                action()
            }
        } else {
            val batchSize = 100
            UniversalArgumentHandler.repeating = true
            repeat(times / batchSize) {
                doRepeat(batchSize, action)
            }
            doRepeat(times % batchSize, action)
            invokeLater { UniversalArgumentHandler.repeating = false }
        }
    }

    private fun doRepeat(times: Int, action: () -> Unit) {
        if (times > 0) {
            invokeLater {
                repeat(times) {
                    if (UniversalArgumentHandler.repeating) {
                        try {
                            action()
                        } catch (_: Exception) {
                            UniversalArgumentHandler.repeating = false
                        }
                    }
                }
            }
        }
    }

    private fun editorActions(): List<String> {
        val actionManager = ActionManager.getInstance()
        return actionManager.getActionIdList("").filter { actionId ->
            actionId !in universalCommandIds &&
                !actionManager.isGroup(actionId) &&
                actionManager.getAction(actionId)?.let { it is EditorAction } == true
        }
    }

    private fun cancel() {
        ui.cancelUI()
    }

    private fun unregisterHandlers() {
        TypedAction.getInstance().apply {
            setupRawHandler(typedHandler.originalHandler)
        }
        EditorActionManager.getInstance().apply {
            actionHandlers.forEach {
                setActionHandler(it.actionId, it.originalHandler)
            }
        }
    }
}
