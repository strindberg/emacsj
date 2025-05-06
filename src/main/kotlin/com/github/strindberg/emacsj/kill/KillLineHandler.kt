package com.github.strindberg.emacsj.kill

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil
import org.intellij.lang.annotations.Language

enum class KillType { REST_OF_LINE, WHOLE_LINE }

@Language("devkit-action-id")
internal const val ACTION_KILL_LINE = "com.github.strindberg.emacsj.actions.kill.line"

@Language("devkit-action-id")
internal const val ACTION_KILL_WHOLE_LINE = "com.github.strindberg.emacsj.actions.kill.wholeline"

class KillLineHandler(val type: KillType) : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        when (type) {
            KillType.REST_OF_LINE -> {
                KillUtil.cut(
                    editor,
                    getStartOffset(caret.offset, editor.document),
                    getEndOffset(caret.offset, editor.document)
                )
            }
            KillType.WHOLE_LINE -> {
                KillUtil.cut(
                    editor,
                    DocumentUtil.getLineStartOffset(caret.offset, editor.document),
                    minOf(editor.document.textLength, DocumentUtil.getLineEndOffset(caret.offset, editor.document) + 1),
                )
            }
        }
    }

    private fun getStartOffset(offset: Int, document: Document): Int = minOf(document.textLength - 1, offset)

    private fun getEndOffset(offset: Int, document: Document): Int {
        val endOffset = DocumentUtil.getLineEndOffset(offset, document)
        return minOf(
            document.textLength - 1,
            if (CharArrayUtil.isEmptyOrSpaces(document.charsSequence, offset, endOffset)) {
                endOffset + 1
            } else if (DocumentUtil.isAtLineStart(offset, document)) {
                endOffset + 1
            } else {
                endOffset
            }
        )
    }
}
