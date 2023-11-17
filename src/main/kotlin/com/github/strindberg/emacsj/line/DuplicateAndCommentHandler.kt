package com.github.strindberg.emacsj.line

import java.lang.invoke.MethodHandles
import com.intellij.codeInsight.generation.CommentByLineCommentHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.DuplicateAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.DocumentUtil

class DuplicateAndCommentHandler : EditorWriteActionHandler.ForEachCaret() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    override fun executeWriteAction(editor: Editor, caret: Caret, dataContext: DataContext) {
        editor.project?.let { project ->
            PsiDocumentManager.getInstance(project).getPsiFile(editor.document)?.let { psiFile ->
                val (start, end, useNewLine) = if (caret.hasSelection()) {
                    Triple(
                        caret.selectionStart,
                        caret.selectionEnd,
                        caret.selectionEnd != DocumentUtil.getLineStartOffset(caret.selectionEnd, editor.document)
                    )
                } else {
                    Triple(
                        DocumentUtil.getLineStartOffset(caret.offset, editor.document),
                        DocumentUtil.getLineEndOffset(caret.offset, editor.document),
                        false,
                    )
                }

                DuplicateAction.duplicateLineOrSelectedBlockAtCaret(editor)

                if (useNewLine) { // If selection did not end at line start
                    editor.document.insertString(end, "\n")
                }

                caret.setSelection(start, end)

                CommentByLineCommentHandler().apply {
                    invoke(project, editor, caret, psiFile)
                    postInvoke()
                }

                caret.removeSelection()
            }
        }
    }
}
