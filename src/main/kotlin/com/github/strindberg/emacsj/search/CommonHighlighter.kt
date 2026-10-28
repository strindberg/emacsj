package com.github.strindberg.emacsj.search

import kotlin.time.Duration.Companion.milliseconds
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting

internal val HIGHLIGHT_DELAY = 50.milliseconds

private const val HIGHLIGHT_CHUNK_SIZE = 100

internal data class SearchRequest(
    val editor: Editor,
    val project: Project,
    val searchArg: String,
    val useRegexp: Boolean,
    val useCase: Boolean,
    val range: IntRange? = null,
    val callback: (List<FindResult>) -> Unit = {},
    val useHighlight: Boolean = true,
)

/** Runs a search off the EDT and paints its matches on it. */
@Service
internal class CommonHighlighter(private val scope: CoroutineScope) {

    /**
     * Debounce applied before a search runs. Overridable purely so that tests need not wait it out on every
     * keystroke; nothing in production changes it.
     */
    @VisibleForTesting
    internal var delay = HIGHLIGHT_DELAY

    // At most one search is ever current: every new one supersedes the last. Painting happens inside the job, so
    // the job being active is what "this search still matters" means -- cancelling it stops the painting too.
    private var search: Job? = null

    // The search painting the current highlights, while it is still at it. It differs from [search] only when a search
    // that paints nothing, such as the count after a repeated C-s, has superseded it.
    private var painting: Pair<Job, SearchRequest>? = null

    internal val isIdle: Boolean
        @VisibleForTesting get() = search?.isActive != true && painting?.run { first.isActive } != true

    internal fun findAllAndHighlight(request: SearchRequest) {
        // A search that paints nothing relies on the highlights of the one before. Cancelling that one before it has
        // painted them would leave the matches unhighlighted, so it is left to finish.
        val stillPainting = painting?.takeIf { (job, painted) -> job.isActive && !request.useHighlight && painted.isSameSearch(request) }
        if (stillPainting != null) {
            search?.takeIf { it !== stillPainting.first }?.cancel()
        } else {
            cancelPending()
        }

        val job = scope.launch {
            delay(delay)

            // An edit after the scan, e.g. a replace shortening the text, moves every offset, and painting matches could be out of range.
            val (stamp, matches) = readAction {
                Pair(request.editor.document.modificationStamp, findAll(request))
            }

            if (request.useHighlight) {
                // A chunk at a time. Painting every match in one go blocks the editor for as long as it takes,
                // which in a large file is long enough that the next keystroke has to wait.
                matches.chunked(HIGHLIGHT_CHUNK_SIZE).forEach { chunk ->
                    onEdt(request, stamp) { chunk.forEach { addHighlight(request.editor, it) } }
                }
            }
            // A search left to finish its painting has been superseded, and the results reported are the newer ones.
            val self = coroutineContext.job
            onEdt(request, stamp) { if (search === self) request.callback(matches) }
        }
        search = job
        if (request.useHighlight) {
            painting = Pair(job, request)
        }
    }

    internal fun cancelPending() {
        search?.cancel()
        search = null
        painting?.run { first.cancel() }
        painting = null
    }

    private fun findAll(request: SearchRequest): List<FindResult> =
        with(request) {
            buildList {
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
                        // Cancellation of a read action is only a request, so without this the scan runs to the end regardless,
                        // holding the read lock against every EDT write -- the next keystroke among them.
                        ProgressManager.checkCanceled()
                        val result = findManager.findString(text, offset, findModel)
                        if (!result.isStringFound) break
                        add(result)
                        offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
                    }
                }
            }
        }

    /**
     * Runs [action] on the EDT as part of this search, so that cancelling the search cancels the painting as well.
     * [stamp] is what says the matches still describe the document; the editor may also be gone by now.
     */
    private suspend fun onEdt(request: SearchRequest, stamp: Long, action: () -> Unit) {
        withContext(Dispatchers.EDT) {
            if (!request.editor.isDisposed && request.editor.document.modificationStamp == stamp) {
                action()
            }
        }
    }

    private fun SearchRequest.isSameSearch(other: SearchRequest): Boolean =
        editor == other.editor && searchArg == other.searchArg && useRegexp == other.useRegexp && useCase == other.useCase &&
            range == other.range

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

    companion object {
        internal val instance: CommonHighlighter
            get() = ApplicationManager.getApplication().getService(CommonHighlighter::class.java)
    }
}
