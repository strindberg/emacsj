package com.github.strindberg.emacsj.space

import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.util.DocumentUtil

internal const val ACTION_DELETE_LINES = "com.github.strindberg.emacsj.actions.space.deletelines"

class DeleteLinesHandler : EditorWriteActionHandler.ForEachCaret() {

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val document = editor.document
        val lineNum = document.getLineNumber(caret.offset)

        if (!DocumentUtil.isLineEmpty(document, lineNum)) {
            if (lineNum + 1 < document.lineCount) {
                val (start, end) = getEmptySuccessorLines(document, lineNum)
                end?.let { document.deleteString(start, minOf(it, editor.text.length)) }
            }
        } else {
            val nextBlank = lineNum + 1 < document.lineCount && DocumentUtil.isLineEmpty(document, lineNum + 1)
            if (!nextBlank && (lineNum == 0 || !DocumentUtil.isLineEmpty(document, lineNum - 1))) {
                document.deleteString(
                    document.getLineStartOffset(lineNum),
                    minOf(document.getLineEndOffset(lineNum) + 1, editor.text.length)
                )
            } else {
                if (lineNum + 1 < document.lineCount) {
                    val (start, end) = getEmptySuccessorLines(document, lineNum)
                    end?.let {
                        document.deleteString(start, minOf(it, editor.text.length))
                        if (caret.offset == editor.text.lastIndex) {
                            document.deleteString(caret.offset, caret.offset + 1)
                        }
                    }
                }
                if (lineNum > 0) {
                    val (start, end) = getEmptyPrecedingLines(document, lineNum)
                    start?.let { document.deleteString(maxOf(0, it), end) }
                }
            }
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
    }

    private fun getEmptySuccessorLines(document: Document, lineNum: Int): Pair<Int, Int?> {
        tailrec fun findEnd(lineNum: Int, end: Int?): Int? =
            if (lineNum < document.lineCount && DocumentUtil.isLineEmpty(document, lineNum)) {
                findEnd(lineNum + 1, document.getLineEndOffset(lineNum) + 1)
            } else {
                end
            }
        return Pair(document.getLineStartOffset(lineNum + 1), findEnd(lineNum + 1, null))
    }

    private fun getEmptyPrecedingLines(document: Document, lineNum: Int): Pair<Int?, Int> {
        tailrec fun findStart(lineNum: Int, start: Int?): Int? =
            if (lineNum > 0 && DocumentUtil.isLineEmpty(document, lineNum)) {
                findStart(lineNum - 1, document.getLineStartOffset(lineNum))
            } else {
                start
            }
        return Pair(findStart(lineNum, null), document.getLineEndOffset(lineNum - 1) + 1)
    }
}
