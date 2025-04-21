package com.github.strindberg.emacsj.space

import com.github.strindberg.emacsj.space.Type.DELETE
import com.github.strindberg.emacsj.space.Type.ONE_SPACE
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler

enum class Type { DELETE, ONE_SPACE }

internal const val ACTION_DELETE_SPACE = "com.github.strindberg.emacsj.actions.space.deletespace"
internal const val ACTION_ONE_SPACE = "com.github.strindberg.emacsj.actions.space.onespace"

class DeleteSpaceHandler(val type: Type) : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val start = previousNonWhiteSpace(editor.text, caret.offset)
        val end = nextNonWhiteSpace(editor.text, caret.offset)
        when (type) {
            DELETE -> {
                editor.document.deleteString(start, end)
            }
            ONE_SPACE -> {
                editor.document.replaceString(start, end, " ")
                if (start >= caret.offset) {
                    caret.moveToOffset(caret.offset + 1)
                }
            }
        }
    }

    private fun previousNonWhiteSpace(text: CharSequence, offset: Int): Int {
        tailrec fun previous(offset: Int): Int =
            if (offset <= 0) {
                0
            } else if (text[offset].isTrueWhitespace()) {
                previous(offset - 1)
            } else {
                offset + 1
            }
        return previous(offset - 1)
    }

    private fun nextNonWhiteSpace(text: CharSequence, offset: Int): Int {
        tailrec fun next(offset: Int): Int =
            if (offset >= text.length) {
                text.length
            } else if (text[offset].isTrueWhitespace()) {
                next(offset + 1)
            } else {
                offset
            }
        return next(offset)
    }

    private fun Char.isTrueWhitespace() = isWhitespace() && this != '\n'
}
