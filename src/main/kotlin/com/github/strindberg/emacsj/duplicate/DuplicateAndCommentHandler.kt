package com.github.strindberg.emacsj.duplicate

import java.lang.invoke.MethodHandles
import com.github.strindberg.emacsj.duplicate.Type.COMMENT
import com.github.strindberg.emacsj.duplicate.Type.DUPLICATE
import com.github.strindberg.emacsj.duplicate.Type.DWIM
import com.github.strindberg.emacsj.word.substring
import com.intellij.codeInsight.generation.CommentByBlockCommentHandler
import com.intellij.codeInsight.generation.CommentByLineCommentHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.DocumentUtil
import com.intellij.util.DocumentUtil.isAtLineEnd
import com.intellij.util.DocumentUtil.isAtLineStart

enum class Type { DUPLICATE, COMMENT, DWIM }

class DuplicateAndCommentHandler(val type: Type) : EditorWriteActionHandler.ForEachCaret() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val document = editor.document

        when (type) {
            DUPLICATE -> {
                if (caret.hasSelection()) {
                    document.insertString(caret.selectionEnd, document.substring(caret.selectionStart, caret.selectionEnd))
                } else {
                    insertLine(caret, document)
                }
            }
            COMMENT -> {
                val useLineComment = useLineComment(caret, document)

                if (caret.hasSelection()) {
                    val start = caret.selectionStart
                    val end = caret.selectionEnd
                    document.insertString(end, document.substring(start, end))
                    caret.setSelection(start, end)
                } else {
                    insertLine(caret, document)
                }

                commentDwim(editor, caret, useLineComment)
            }
            DWIM -> {
                val useLineComment = useLineComment(caret, document)
                commentDwim(editor, caret, useLineComment)
            }
        }

        caret.removeSelection()
    }

    private fun useLineComment(caret: Caret, document: Document) =
        if (caret.hasSelection()) {
            isAtLineStart(caret.selectionStart, document) &&
                (isAtLineStart(caret.selectionEnd, document) || isAtLineEnd(caret.selectionEnd, document))
        } else {
            true
        }

    private fun commentDwim(
        editor: Editor,
        caret: Caret,
        useLineComment: Boolean,
    ) {
        editor.project?.let { project ->
            PsiDocumentManager.getInstance(project).getPsiFile(editor.document)?.let { psiFile ->
                if (useLineComment) {
                    CommentByLineCommentHandler().apply {
                        invoke(project, editor, caret, psiFile)
                        postInvoke()
                    }
                } else {
                    CommentByBlockCommentHandler().apply {
                        invoke(project, editor, caret, psiFile)
                        postInvoke()
                    }
                }
            }
        }
    }

    private fun insertLine(caret: Caret, document: Document) {
        val lineStart = DocumentUtil.getLineStartOffset(caret.offset, document)
        val lineEnd = DocumentUtil.getLineEndOffset(caret.offset, document)
        if (lineEnd >= document.textLength) {
            document.insertString(document.textLength, "\n")
        }
        document.insertString(lineEnd + 1, document.substring(lineStart, lineEnd) + "\n")
    }
}
