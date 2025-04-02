package com.github.strindberg.emacsj.search

import kotlin.concurrent.thread
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
import org.jetbrains.annotations.VisibleForTesting

object CommonHighlighter {

    @VisibleForTesting
    internal var testing = false

    private var progressIndicators = mutableListOf<ProgressIndicator>()

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
        if (testing) {
            doFindAllAndHighlight(editor, searchArg, useRegexp, useCase, range, callback, highlight)
        } else {
            val indicator = ProgressIndicatorBase()
            progressIndicators.add(indicator)
            thread {
                ProgressManager.getInstance()
                    .runProcess(
                        {
                            Thread.sleep(50)
                            doFindAllAndHighlight(editor, searchArg, useRegexp, useCase, range, callback, highlight)
                        },
                        indicator
                    )
            }
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
    ) {
        val matches = mutableListOf<FindResult>()
        if (searchArg.isNotEmpty()) {
            val findManager = FindManager.getInstance(editor.project)
            val findModel = FindModel().apply {
                stringToFind = searchArg
                isCaseSensitive = useCase
                isRegularExpressions = useRegexp
            }
            val text = editor.text.substring(0, range?.last ?: editor.text.length)
            var offset = range?.start ?: 0

            if (!testing) {
                ProgressManager.checkCanceled()
            }
            while (offset < text.length) {
                val result = findManager.findString(text, offset, findModel)
                if (!result.isStringFound) break
                matches.add(result)
                offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
            }

            if (highlight) {
                addSecondaryHighlights(editor, matches)
            }
        }
        callback(matches)
    }

    private fun addSecondaryHighlights(editor: Editor, matches: List<FindResult>) {
        if (testing) {
            matches.forEach { match ->
                addHighlight(editor, match)
            }
        } else {
            matches.chunked(100).forEach { chunk ->
                ProgressManager.checkCanceled()
                ApplicationManager.getApplication().invokeLater {
                    chunk.forEach { match ->
                        addHighlight(editor, match)
                    }
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
