package com.github.strindberg.emacsj.kill

import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import kotlin.concurrent.thread
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import com.github.strindberg.emacsj.search.EMACSJ_SECONDARY
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.util.DocumentUtil
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting

@Language("devkit-action-id")
internal const val ACTION_COPY = "com.github.strindberg.emacsj.actions.kill.copy"

class CopyRegionHandler : EditorActionHandler() {

    companion object {
        @VisibleForTesting
        internal var isTesting = false
    }

    private var lastInvocation = OffsetDateTime.MIN

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        if (editor.selectionModel.hasSelection()) {
            KillUtil.copy(
                editor = editor,
                textStartOffset = editor.selectionModel.selectionStart,
                textEndOffset = editor.selectionModel.selectionEnd,
                prepend = primary.offset == editor.selectionModel.selectionStart,
            )
        } else if (notThrottled()) {
            val textStart = DocumentUtil.getLineStartOffset(primary.offset, editor.document)
            val textEnd = minOf(editor.document.textLength, DocumentUtil.getLineEndOffset(primary.offset, editor.document) + 1)
            KillUtil.copy(
                editor = editor,
                textStartOffset = textStart,
                textEndOffset = textEnd,
            )
            val highlighter = editor.markupModel.addRangeHighlighter(
                EMACSJ_SECONDARY,
                textStart,
                textEnd,
                HighlighterLayer.LAST + 1,
                HighlighterTargetArea.EXACT_RANGE
            )
            thread {
                Thread.sleep(500)
                ApplicationManager.getApplication().invokeLater {
                    editor.markupModel.removeHighlighter(highlighter)
                }
            }
        }
        lastInvocation = now()
    }

    // Avoid inadvertently running the command multiple times because of key repeat.
    private fun notThrottled(): Boolean = isTesting || lastInvocation.isBefore(now().minus(200.milliseconds.toJavaDuration()))
}
