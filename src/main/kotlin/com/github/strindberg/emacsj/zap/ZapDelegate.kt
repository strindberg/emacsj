package com.github.strindberg.emacsj.zap

import java.awt.event.KeyEvent
import java.util.UUID
import com.github.strindberg.emacsj.kill.KillUtil
import com.github.strindberg.emacsj.search.CommonUI
import com.github.strindberg.emacsj.search.RestorableActionHandler
import com.github.strindberg.emacsj.search.RestorableTypedActionHandler
import com.github.strindberg.emacsj.word.text
import com.github.strindberg.emacsj.zap.ZapType.BACKWARD_TO
import com.github.strindberg.emacsj.zap.ZapType.BACKWARD_UP_TO
import com.github.strindberg.emacsj.zap.ZapType.FORWARD_TO
import com.github.strindberg.emacsj.zap.ZapType.FORWARD_UP_TO
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import org.jetbrains.annotations.VisibleForTesting

class ZapDelegate(val editor: Editor, val type: ZapType) {
    // Prevent dead keys such as '^' and '~' from showing in editor by setting document to read only.
    private val document: Document = this.editor.document.apply { setReadOnly(true) }

    private val typedHandler: RestorableTypedActionHandler

    private val actionHandlers = mutableListOf<RestorableActionHandler<ZapDelegate>>()

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::keyEventHandler, ::hide)

    init {
        ui.title = when (type) {
            FORWARD_TO -> "Zap to char: "
            FORWARD_UP_TO -> "Zap up to char: "
            BACKWARD_TO -> "Zap back to char: "
            BACKWARD_UP_TO -> "Zap back up to char: "
        }

        TypedAction.getInstance().apply {
            setupRawHandler(
                object : RestorableTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        val undoGroupId = UUID.randomUUID().toString()
                        document.setReadOnly(false)
                        editor.caretModel.allCarets.reversed().forEach { caret ->
                            val (start, end) = when (type) {
                                FORWARD_TO, FORWARD_UP_TO -> Pair(caret.offset, nextCharacter(editor.text, caret.offset, charTyped))
                                BACKWARD_TO, BACKWARD_UP_TO -> Pair(previousCharacter(editor.text, caret.offset, charTyped), caret.offset)
                            }
                            WriteCommandAction.runWriteCommandAction(editor.project, "Zap ${type.name.lowercase()}", undoGroupId, {
                                KillUtil.cut(editor, start, end, prepend = type in listOf(BACKWARD_TO, BACKWARD_UP_TO))
                            })
                        }
                        cancel()
                    }
                }.also { typedHandler = it }
            )
        }

        EditorActionManager.getInstance().apply {
            editorActions().forEach { actionId ->
                getActionHandler(actionId)?.let { originalHandler ->
                    setActionHandler(
                        actionId,
                        RestorableActionHandler(actionId, originalHandler, { ZapHandler.delegate }) { caret, dataContext ->
                            cancel()
                            originalHandler.execute(editor, caret, dataContext)
                        }.also { actionHandlers.add(it) }
                    )
                }
            }
        }

        ui.show()
    }

    internal fun hide(): Boolean {
        unregisterHandlers()

        document.setReadOnly(false)

        ui.cancelUI()

        ZapHandler.delegate = null

        return true
    }

    @Suppress("UnusedParameter")
    private fun keyEventHandler(e: KeyEvent): Boolean {
        cancel()
        return false
    }

    private fun editorActions(): List<String> {
        val actionManager = ActionManager.getInstance()
        return actionManager.getActionIdList("").filter { actionId ->
            !actionManager.isGroup(actionId) && actionManager.getAction(actionId)?.let { it is EditorAction } == true
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
                setActionHandler(it.action, it.originalHandler)
            }
        }
    }

    private fun nextCharacter(text: CharSequence, offset: Int, character: Char): Int {
        tailrec fun next(offset: Int): Int =
            if (offset >= text.length) {
                text.length
            } else if (matches(text[offset], character)) {
                if (type == FORWARD_UP_TO) {
                    offset
                } else {
                    minOf(text.length, offset + 1)
                }
            } else {
                next(offset + 1)
            }
        return next(offset)
    }

    private fun previousCharacter(text: CharSequence, offset: Int, character: Char): Int {
        tailrec fun previous(offset: Int): Int =
            if (offset <= 0) {
                0
            } else if (matches(text[offset - 1], character)) {
                if (type == BACKWARD_UP_TO) {
                    offset
                } else {
                    maxOf(0, offset - 1)
                }
            } else {
                previous(offset - 1)
            }
        return previous(offset)
    }
}

private fun matches(charAtOffset: Char, givenChar: Char) =
    if (givenChar.isUpperCase()) charAtOffset == givenChar else givenChar.lowercaseChar() == charAtOffset.lowercaseChar()
