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

private data class SearchRequest(
    val editor: Editor,
    val project: Project,
    val searchArg: String,
    val useRegexp: Boolean,
    val useCase: Boolean,
    val range: IntRange?,
    val callback: (List<FindResult>) -> Unit,
    val highlight: Boolean,
)

object CommonHighlighter {

    /**
     * Debounce applied before a scheduled search runs. Overridable purely so that tests need not wait it out on
     * every keystroke; nothing in production changes it.
     */
    @VisibleForTesting
    internal var delayMillis = HIGHLIGHT_DELAY_MILLIS

    private val progressIndicators = mutableListOf<ProgressIndicator>()

    private val scheduledSearches = mutableListOf<ScheduledFuture<*>>()

    internal val isIdle: Boolean
        @VisibleForTesting get() = scheduledSearches.all { it.isDone }

    internal fun cancel(editor: Editor) {
        progressIndicators.forEach { it.cancel() }
        progressIndicators.clear()

        editor.markupModel.removeAllHighlighters()
    }

    internal fun findAllAndHighlight(
        editor: Editor,
        project: Project,
        searchArg: String,
        useRegexp: Boolean,
        useCase: Boolean,
        range: IntRange? = null,
        callback: (List<FindResult>) -> Unit = {},
        highlight: Boolean = true,
    ) {
        // A new search supersedes any still-pending one. Without this, two searches scheduled inside the debounce
        // window run concurrently on the pool and can report back out of order, leaving a stale match count.
        progressIndicators.forEach { it.cancel() }
        progressIndicators.clear()

        val indicator = ProgressIndicatorBase()
        progressIndicators.add(indicator)
        scheduledSearches.removeAll { it.isDone }
        scheduledSearches.add(
            AppExecutorUtil.getAppScheduledExecutorService().schedule(
                {
                    ProgressManager.getInstance().runProcess(
                        {
                            ApplicationManager.getApplication().runReadAction {
                                doFindAllAndHighlight(
                                    SearchRequest(
                                        editor = editor,
                                        project = project,
                                        searchArg = searchArg,
                                        useRegexp = useRegexp,
                                        useCase = useCase,
                                        range = range,
                                        callback = callback,
                                        highlight = highlight
                                    ),
                                    indicator
                                )
                            }
                        },
                        indicator
                    )
                },
                delayMillis,
                TimeUnit.MILLISECONDS
            )
        )
    }

    private fun doFindAllAndHighlight(request: SearchRequest, indicator: ProgressIndicator) {
        with(request) {
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

                ProgressManager.checkCanceled()
                while (offset < text.length) {
                    val result = findManager.findString(text, offset, findModel)
                    if (!result.isStringFound) break
                    matches.add(result)
                    offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
                }

                if (highlight) {
                    addSecondaryHighlights(editor, matches, indicator)
                }
            }
            onEdt(editor, indicator) { callback(matches) }
        }
    }

    private fun addSecondaryHighlights(editor: Editor, matches: List<FindResult>, indicator: ProgressIndicator) {
        matches.chunked(HIGHLIGHT_CHUNK_SIZE).forEach { chunk ->
            ProgressManager.checkCanceled()
            onEdt(editor, indicator) {
                chunk.forEach { match ->
                    addHighlight(editor, match)
                }
            }
        }
    }

    /**
     * Runs [action] on the EDT, re-checking cancellation there. A checkCanceled() on the background thread only
     * proves the search was live when the task was queued: cancel() can still run on the EDT in between, clearing
     * the highlighters, after which the queued task would paint stale matches back in.
     */
    private fun onEdt(editor: Editor, indicator: ProgressIndicator, action: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            if (!editor.isDisposed && !indicator.isCanceled) {
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
