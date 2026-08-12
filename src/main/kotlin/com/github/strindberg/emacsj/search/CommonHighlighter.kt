package com.github.strindberg.emacsj.search

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.annotations.VisibleForTesting

internal const val HIGHLIGHT_DELAY_MILLIS = 50L

private const val HIGHLIGHT_CHUNK_SIZE = 100

internal data class SearchRequest(
    val editor: Editor,
    val project: Project,
    val searchArg: String,
    val useRegexp: Boolean,
    val useCase: Boolean,
    val range: IntRange? = null,
    val callback: (List<FindResult>) -> Unit = {},
    val highlight: Boolean = true,
)

object CommonHighlighter {

    /**
     * Debounce applied before a scheduled search runs. Overridable purely so that tests need not wait it out on
     * every keystroke; nothing in production changes it.
     */
    @VisibleForTesting
    internal var delayMillis = HIGHLIGHT_DELAY_MILLIS

    // At most one search is ever in flight: every new one supersedes the last
    private var indicator: ProgressIndicator? = null

    private var scheduledSearch: ScheduledFuture<*>? = null

    internal val isIdle: Boolean
        @VisibleForTesting get() = scheduledSearch?.isDone != false

    internal fun findAllAndHighlight(request: SearchRequest) {
        // A new search supersedes any still-pending one. Without this, two searches scheduled inside the debounce
        // window run concurrently on the pool and can report back out of order, leaving a stale match count.
        cancelPending()

        val searchIndicator = ProgressIndicatorBase().apply { indicator = this }
        scheduledSearch = AppExecutorUtil.getAppScheduledExecutorService().schedule(
            {
                ProgressManager.getInstance().runProcess(
                    {
                        ApplicationManager.getApplication().runReadAction { doFindAllAndHighlight(request, searchIndicator) }
                    },
                    searchIndicator
                )
            },
            delayMillis,
            TimeUnit.MILLISECONDS
        )
    }

    internal fun cancelPending() {
        // Canceling the indicator only turns a search that is already running into a no-op; canceling the future
        // stops one that is still inside the debounce window from running at all.
        indicator?.cancel()
        indicator = null
        scheduledSearch?.cancel(false)
        scheduledSearch = null
    }

    private fun doFindAllAndHighlight(request: SearchRequest, indicator: ProgressIndicator) {
        with(request) {
            // Matches are offsets into the document as it was when the scan ran. An edit in between -- e.g. replace
            // shortening the text -- invalidates every one of them, and painting them would be out of range.
            val stamp = editor.document.modificationStamp
            val matches = mutableListOf<FindResult>()
            if (searchArg.isNotEmpty()) {
                val findManager = FindManager.getInstance(project)
                val findModel = FindModel().apply {
                    stringToFind = searchArg
                    isCaseSensitive = useCase
                    isRegularExpressions = useRegexp
                }
                val documentText = editor.text
                val text = documentText.substring(0, minOf(range?.last ?: documentText.length, documentText.length))
                var offset = range?.start ?: 0

                while (offset < text.length) {
                    ProgressManager.checkCanceled()
                    val result = findManager.findString(text, offset, findModel)
                    if (!result.isStringFound) break
                    matches.add(result)
                    offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
                }

                if (highlight) {
                    addSecondaryHighlights(editor, matches, indicator, stamp)
                }
            }
            onEdt(editor, indicator, stamp) { callback(matches) }
        }
    }

    private fun addSecondaryHighlights(
        editor: Editor,
        matches: List<FindResult>,
        indicator: ProgressIndicator,
        stamp: Long,
    ) {
        matches.chunked(HIGHLIGHT_CHUNK_SIZE).forEach { chunk ->
            ProgressManager.checkCanceled()
            // Chunks land on the EDT one at a time, so the caller's list grows as they arrive. A chunk queued before the search was
            // canceled is skipped by onEdt, which is what keeps the list in step with what is actually painted.
            onEdt(editor, indicator, stamp) {
                chunk.forEach { match ->
                    addHighlight(editor, match)
                }
            }
        }
    }

    /**
     * Runs [action] on the EDT, re-checking cancellation there. A checkCanceled() on the background thread only
     * proves the search was live when the task was queued: cancel() can still run on the EDT in between, clearing
     * the highlighters, after which the queued task would paint stale matches back in. [stamp] does the same for
     * the document: an edit between scanning and painting leaves the matches pointing at text that has moved.
     */
    private fun onEdt(editor: Editor, indicator: ProgressIndicator, stamp: Long, action: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            if (!editor.isDisposed && !indicator.isCanceled && editor.document.modificationStamp == stamp) {
                action()
            }
        }
    }

    private fun addHighlight(editor: Editor, match: FindResult) {
        if (!match.isEmpty) {
            editor.markupModel.addRangeHighlighter(
                EMACSJ_SECONDARY,
                match.startOffset,
                match.endOffset,
                HighlighterLayer.LAST + 1,
                HighlighterTargetArea.EXACT_RANGE
            )
        }
    }
}
