package com.github.strindberg.emacsj.search

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
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.annotations.VisibleForTesting

private const val HIGHLIGHT_DELAY_MILLIS = 50L

private const val HIGHLIGHT_CHUNK_SIZE = 100

object CommonHighlighter {

    @VisibleForTesting
    internal var isTesting = false

    private val progressIndicators = mutableListOf<ProgressIndicator>()

    internal fun cancel(editor: Editor) {
        val iterator = progressIndicators.iterator()
        while (iterator.hasNext()) {
            iterator.next().cancel()
            iterator.remove()
        }
        editor.markupModel.removeAllHighlighters()
    }

    internal fun findAllAndHighlight(
        editor: Editor,
        searchArg: String,
        useRegexp: Boolean,
        useCase: Boolean,
        range: IntRange? = null,
        callback: (List<FindResult>) -> Unit = {},
        highlight: Boolean = true,
    ) {
        if (isTesting) {
            doFindAllAndHighlight(
                editor = editor,
                searchArg = searchArg,
                useRegexp = useRegexp,
                useCase = useCase,
                range = range,
                callback = callback,
                highlight = highlight,
                indicator = null
            )
        } else {
            val indicator = ProgressIndicatorBase()
            progressIndicators.add(indicator)
            AppExecutorUtil.getAppScheduledExecutorService().schedule(
                {
                    ProgressManager.getInstance().runProcess(
                        {
                            ApplicationManager.getApplication().runReadAction {
                                doFindAllAndHighlight(
                                    editor = editor,
                                    searchArg = searchArg,
                                    useRegexp = useRegexp,
                                    useCase = useCase,
                                    range = range,
                                    callback = callback,
                                    highlight = highlight,
                                    indicator = indicator
                                )
                            }
                        },
                        indicator
                    )
                },
                HIGHLIGHT_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun doFindAllAndHighlight(
        editor: Editor,
        searchArg: String,
        useRegexp: Boolean,
        useCase: Boolean,
        range: IntRange?,
        callback: (List<FindResult>) -> Unit,
        highlight: Boolean,
        indicator: ProgressIndicator?,
    ) {
        val matches = mutableListOf<FindResult>()
        if (searchArg.isNotEmpty()) {
            val findManager = FindManager.getInstance(editor.project)
            val findModel = FindModel().apply {
                stringToFind = searchArg
                isCaseSensitive = useCase
                isRegularExpressions = useRegexp
            }
            val documentText = editor.text
            val text = documentText.substring(0, minOf(range?.last ?: documentText.length, documentText.length))
            var offset = range?.start ?: 0

            if (!isTesting) {
                ProgressManager.checkCanceled()
            }
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

    private fun addSecondaryHighlights(editor: Editor, matches: List<FindResult>, indicator: ProgressIndicator?) {
        matches.chunked(HIGHLIGHT_CHUNK_SIZE).forEach { chunk ->
            if (!isTesting) {
                ProgressManager.checkCanceled()
            }
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
    @Suppress("CanBeNonNullable")
    private fun onEdt(editor: Editor, indicator: ProgressIndicator?, action: () -> Unit) {
        if (isTesting) {
            action()
        } else {
            ApplicationManager.getApplication().invokeLater {
                if (!editor.isDisposed && indicator?.isCanceled != true) {
                    action()
                }
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
