package com.github.strindberg.emacsj.kill

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.KillRingUtil
import com.intellij.util.DocumentUtil
import com.intellij.util.text.CharArrayUtil


class KillHandler : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        KillRingUtil.cut(editor, caret.offset, getEndOffset(caret.offset, editor.document))
    }

    private fun getEndOffset(offset: Int, document: Document): Int {
        val endOffset = DocumentUtil.getLineEndOffset(offset, document)
        return minOf(
            document.textLength - 1,
            if (CharArrayUtil.isEmptyOrSpaces(document.charsSequence, offset, endOffset)) {
                endOffset + 1
            } else if (offset == DocumentUtil.getLineStartOffset(offset, document)) {
                endOffset + 1
            } else {
                endOffset
            }
        )
    }

}
