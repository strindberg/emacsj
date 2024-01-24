package com.github.strindberg.emacsj.search

import kotlin.concurrent.thread
import com.github.strindberg.emacsj.search.SearchType.REGEXP
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

class CommonHighlighter {

    companion object {
        @VisibleForTesting
        internal var testing = false

        private var progressIndicator: ProgressIndicator? = null

        internal fun cancel() {
            progressIndicator?.cancel()
        }

        internal fun findAllAndHighlight(
            editor: Editor,
            searchArg: String,
            type: SearchType,
            useCase: Boolean,
            range: IntRange? = null,
            results: List<Int> = emptyList(),
            callback: (List<FindResult>) -> Unit = {},
            reHighlight: Boolean = true,
        ) {
            if (testing) {
                doFindAllAndHighlight(editor, searchArg, type, useCase, range, results, callback, reHighlight)
            } else {
                progressIndicator?.cancel()
                progressIndicator = ProgressIndicatorBase()
                thread {
                    ProgressManager.getInstance()
                        .runProcess(
                            {
                                doFindAllAndHighlight(editor, searchArg, type, useCase, range, results, callback, reHighlight)
                            },
                            progressIndicator
                        )
                }
            }
        }

        private fun doFindAllAndHighlight(
            editor: Editor,
            searchArg: String,
            type: SearchType,
            useCase: Boolean,
            range: IntRange?,
            results: List<Int>,
            callback: (List<FindResult>) -> Unit,
            reHighlight: Boolean,
        ) {
            val matches = mutableListOf<FindResult>()

            if (searchArg.isNotEmpty()) {
                val findManager = FindManager.getInstance(editor.project)
                val findModel = FindModel().apply {
                    stringToFind = searchArg
                    isCaseSensitive = useCase
                    isRegularExpressions = type == REGEXP
                }

                val text = editor.text.substring(0, range?.last ?: editor.text.length)
                var offset = range?.start ?: 0
                var count = 1
                while (offset < text.length) {
                    count = checkCanceled(count)
                    val result = findManager.findString(text, offset, findModel)
                    if (!result.isStringFound) break
                    matches.add(result)
                    offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
                }
                if (reHighlight) {
                    addSecondaryHighlights(editor, matches, results)
                }
            }
            callback(matches)
        }

        private fun addSecondaryHighlights(editor: Editor, matches: List<FindResult>, results: List<Int>) {
            if (testing) {
                matches.forEach { match ->
                    addHighlight(match, results, editor)
                }
            } else {
                var count = 1
                matches.forEach { match ->
                    count = checkCanceled(count)
                    ApplicationManager.getApplication().invokeLater {
                        addHighlight(match, results, editor)
                    }
                }
            }
        }

        private fun addHighlight(
            match: FindResult,
            results: List<Int>,
            editor: Editor,
        ) {
            if (!match.isEmpty && match.startOffset !in results) {
                editor.markupModel.addRangeHighlighter(
                    EMACSJ_SECONDARY,
                    match.startOffset,
                    match.endOffset,
                    HighlighterLayer.LAST + 1,
                    HighlighterTargetArea.EXACT_RANGE
                )
            }
        }

        private fun checkCanceled(count: Int): Int {
            if (!testing && count % 40 == 0) {
                ProgressManager.checkCanceled()
            }
            return count + 1
        }
    }
}
