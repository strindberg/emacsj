package com.github.strindberg.emacsj.zap

import java.util.UUID
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.kill.KillUtil
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.ui.UIDelegate
import com.github.strindberg.emacsj.ui.constructInput
import com.github.strindberg.emacsj.ui.isActive
import com.github.strindberg.emacsj.word.text
import com.github.strindberg.emacsj.zap.ZapType.BACKWARD_TO
import com.github.strindberg.emacsj.zap.ZapType.BACKWARD_UP_TO
import com.github.strindberg.emacsj.zap.ZapType.FORWARD_TO
import com.github.strindberg.emacsj.zap.ZapType.FORWARD_UP_TO
import com.intellij.codeInsight.hint.HintManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.util.EditorUtil
import org.jetbrains.annotations.VisibleForTesting

internal class ZapDelegate(editor: Editor, private val type: ZapType) : UIDelegate(editor) {

    @VisibleForTesting
    override val ui = CommonUI(editor = editor, isWriteable = false, cancelCallback = ::hide).apply {
        title = when (type) {
            FORWARD_TO -> "Zap to char: "
            FORWARD_UP_TO -> "Zap up to char: "
            BACKWARD_TO -> "Zap back to char: "
            BACKWARD_UP_TO -> "Zap back up to char: "
        }
    }

    init {
        EditorUtil.disposeWithEditor(editor, this)

        // Handle dead keys, i.e. accents waiting for their main character. If this dispatcher is not used,
        // typed accents show in the editor until the full character is composed.
        IdeEventQueue.getInstance().addDispatcher(
            { e ->
                val delegate = ZapHandler.delegate
                if (delegate.isActive(e)) {
                    e.constructInput()?.let { delegate.doZap(it.first()) }
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

    internal fun doZap(charTyped: Char) {
        val undoGroupId = UUID.randomUUID().toString()
        val times = EmacsJService.instance.universalArgumentRelaxed()
        editor.caretModel.allCarets.reversed().forEach { caret ->
            val (start, end) = when (type) {
                FORWARD_TO, FORWARD_UP_TO -> Pair(
                    caret.offset,
                    nextCharacter(text = editor.text, startOffset = caret.offset, character = charTyped, times = times)
                )
                BACKWARD_TO, BACKWARD_UP_TO -> Pair(
                    previousCharacter(
                        text = editor.text,
                        startOffset = caret.offset,
                        character = charTyped,
                        times = times
                    ),
                    caret.offset
                )
            }
            if (start != null && end != null) {
                WriteCommandAction.runWriteCommandAction(editor.project, "Zap ${type.name.lowercase()}", undoGroupId, {
                    KillUtil.cut(
                        editor = editor,
                        textStartOffset = start,
                        textEndOffset = end,
                        prepend = type in [BACKWARD_TO, BACKWARD_UP_TO]
                    )
                })
            } else {
                HintManager.getInstance().showInformationHint(editor, "Search failed: $charTyped")
            }
        }
        hide()
    }

    override fun clearDelegate() {
        ZapHandler.delegate = null
    }

    private fun nextCharacter(text: CharSequence, startOffset: Int, character: Char, times: Int): Int? {
        tailrec fun next(offset: Int, found: Int): Int? =
            if (offset >= text.length) {
                null
            } else if (matches(text[offset], character)) {
                if (found < times - 1) {
                    next(offset + 1, found + 1)
                } else {
                    if (type == FORWARD_UP_TO) {
                        offset
                    } else {
                        minOf(text.length, offset + 1)
                    }
                }
            } else {
                next(offset + 1, found)
            }
        return next(startOffset, 0)
    }

    private fun previousCharacter(text: CharSequence, startOffset: Int, character: Char, times: Int): Int? {
        tailrec fun previous(offset: Int, found: Int): Int? =
            if (offset <= 0) {
                null
            } else if (matches(text[offset - 1], character)) {
                if (found < times - 1) {
                    previous(offset - 1, found + 1)
                } else {
                    if (type == BACKWARD_UP_TO) {
                        offset
                    } else {
                        maxOf(0, offset - 1)
                    }
                }
            } else {
                previous(offset - 1, found)
            }
        return previous(startOffset, 0)
    }
}

private fun matches(charAtOffset: Char, givenChar: Char) =
    if (givenChar.isUpperCase()) charAtOffset == givenChar else givenChar.lowercaseChar() == charAtOffset.lowercaseChar()
