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
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import org.jetbrains.annotations.VisibleForTesting

private val singleActions = listOf(
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
)

class UniversalArgumentDelegate(val editor: Editor, val dataContext: DataContext, var numeric: Int?) {

    // Prevent dead keys such as '^' and '~' from showing in editor by setting document to read only.
    private val document: Document = this.editor.document.apply { setReadOnly(true) }

    private val typedHandler: RestorableTypedActionHandler

    private val actionHandlers: List<RestorableActionHandler<UniversalArgumentDelegate>>

    private var counter = 4

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::hide).apply {
        title = "Argument: "
        text = getTimes().toString()
    }

    init {
        TypedAction.getInstance().apply {
            setupRawHandler(
                object : RestorableTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        val delegate = UniversalArgumentHandler.delegate
                        if (delegate != null) {
                            if (charTyped.isDigit()) {
                                val digit = charTyped.digitToInt()
                                addDigit(digit)
                            } else {
                                document.setReadOnly(false)
                                repeat(getTimes()) { myOriginalHandler?.execute(editor, charTyped, dataContext) }
                                cancel()
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
                                document.setReadOnly(false)
                                if (actionId in singleActions) {
                                    originalHandler.execute(editor, caret, dataContext)
                                } else {
                                    repeat(getTimes()) {
                                        originalHandler.execute(editor, caret, dataContext)
                                    }
                                }
                                cancel()
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

    internal fun hide() {
        unregisterHandlers()

        document.setReadOnly(false)

        ui.cancelUI()

        UniversalArgumentHandler.delegate = null
    }

    private fun getTimes(): Int = numeric ?: counter

    private fun editorActions(): List<String> {
        val actionManager = ActionManager.getInstance()
        return actionManager.getActionIdList("").filter { actionId ->
            actionId !in listOf(
                ACTION_UNIVERSAL_ARGUMENT,
                ACTION_UNIVERSAL_ARGUMENT1,
                ACTION_UNIVERSAL_ARGUMENT2,
                ACTION_UNIVERSAL_ARGUMENT3,
                ACTION_UNIVERSAL_ARGUMENT4,
                ACTION_UNIVERSAL_ARGUMENT5,
                ACTION_UNIVERSAL_ARGUMENT6,
                ACTION_UNIVERSAL_ARGUMENT7,
                ACTION_UNIVERSAL_ARGUMENT8,
                ACTION_UNIVERSAL_ARGUMENT9,
                ACTION_UNIVERSAL_ARGUMENT0,
            ) &&
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
