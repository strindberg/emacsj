package com.github.strindberg.emacsj.kill

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import com.github.strindberg.emacsj.EmacsJScope
import com.github.strindberg.emacsj.search.EMACSJ_SECONDARY
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.util.DocumentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting

@Language("devkit-action-id")
internal const val ACTION_COPY = "com.github.strindberg.emacsj.actions.kill.copy"

private val HIGHLIGHT_DURATION = 500L.milliseconds

internal val THROTTLE_DURATION = 200L.milliseconds

internal class CopyRegionHandler : EditorActionHandler() {

    companion object {

        @VisibleForTesting
        internal var timeSource: TimeSource = TimeSource.Monotonic
    }

    private var lastInvocation: TimeMark? = null

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
            EmacsJScope.instance.scope.launch {
                delay(HIGHLIGHT_DURATION)
                withContext(Dispatchers.EDT) { highlighter.dispose() }
            }
        }
        lastInvocation = timeSource.markNow()
    }

    // Avoid inadvertently running the command multiple times because of key repeat.
    private fun notThrottled() = lastInvocation?.run { elapsedNow() >= THROTTLE_DURATION } != false
}
