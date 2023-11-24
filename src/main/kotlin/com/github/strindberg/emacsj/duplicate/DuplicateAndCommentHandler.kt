package com.github.strindberg.emacsj.duplicate

import java.lang.invoke.MethodHandles
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

enum class Type { DUPLICATE, COMMENT }

class DuplicateAndCommentHandler(val type: Type) : EditorWriteActionHandler.ForEachCaret() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        val document = editor.document

        when (type) {
            Type.DUPLICATE -> {
                if (caret.hasSelection()) {
                    val content = document.substring(caret.selectionStart, caret.selectionEnd)
                    document.insertString(caret.selectionEnd, content)
                } else {
                    insertLine(caret, document)
                }
            }

            Type.COMMENT -> {
                val useLineComment =
                    if (caret.hasSelection()) {
                        val start = caret.selectionStart
                        val end = caret.selectionEnd
                        document.insertString(end, document.substring(start, end))
                        caret.setSelection(start, end)
                        DocumentUtil.isAtLineStart(start, document) && DocumentUtil.isAtLineStart(end, document)
                    } else {
                        insertLine(caret, document)
                        true
                    }

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
        }

        caret.removeSelection()
    }

    private fun insertLine(caret: Caret, document: Document) {
        val lineStart = DocumentUtil.getLineStartOffset(caret.offset, document)
        val lineEnd = DocumentUtil.getLineEndOffset(caret.offset, document)
        val content = document.substring(lineStart, lineEnd)
        if (lineEnd >= document.textLength) {
            document.insertString(document.textLength, "\n")
        }
        document.insertString(lineEnd + 1, content + "\n")
    }
}
