package com.github.strindberg.emacsj.space

import java.lang.invoke.MethodHandles
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.util.DocumentUtil

enum class Type { SINGLE, MULTIPLE }

class DeleteSpaceHandler(val type: Type) : EditorWriteActionHandler.ForEachCaret() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        if (type == Type.SINGLE) {
            deleteSpace(editor, caret)
        } else {
            deleteLines(editor, caret)
        }
    }

    private fun deleteSpace(editor: Editor, caret: Caret) {
        val start = previousNonWhiteSpace(editor.text, caret.offset)
        val end = nextNonWhiteSpace(editor.text, caret.offset)
        editor.document.deleteString(start, end)
    }

    private fun deleteLines(editor: Editor, caret: Caret) {
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
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    private fun getEmptySuccessorLines(document: Document, lineNum: Int): Pair<Int, Int?> {
        val start = document.getLineStartOffset(lineNum + 1)
        var end: Int? = null
        var currentLine = lineNum + 1
        while (currentLine < document.lineCount && DocumentUtil.isLineEmpty(document, currentLine)) {
            end = document.getLineEndOffset(currentLine) + 1
            currentLine++
        }
        return Pair(start, end)
    }

    private fun getEmptyPrecedingLines(document: Document, lineNum: Int): Pair<Int?, Int> {
        val end = document.getLineEndOffset(lineNum - 1) + 1
        var start: Int? = null
        var currentLine = lineNum - 1
        while (currentLine > 0 && DocumentUtil.isLineEmpty(document, currentLine)) {
            start = document.getLineStartOffset(currentLine)
            currentLine--
        }
        return Pair(start, end)
    }
}

internal fun previousNonWhiteSpace(text: CharSequence, offset: Int): Int {
    var currentOffset = offset
    while (currentOffset > 0 && text[currentOffset - 1].isTrueWhitespace()) {
        currentOffset -= 1
    }
    return currentOffset
}

internal fun nextNonWhiteSpace(text: CharSequence, offset: Int): Int {
    var currentOffset = offset
    while (currentOffset <= text.lastIndex && text[currentOffset].isTrueWhitespace()) {
        currentOffset += 1
    }
    return currentOffset
}

private fun Char.isTrueWhitespace() = isWhitespace() && this != '\n'
