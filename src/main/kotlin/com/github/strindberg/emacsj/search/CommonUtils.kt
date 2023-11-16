package com.github.strindberg.emacsj.search

import java.lang.Character.isUpperCase
import java.lang.Character.toLowerCase
import java.lang.Character.toUpperCase
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea

enum class SearchType { TEXT, REGEXP }

internal val EMACSJ_PRIMARY = TextAttributesKey.createTextAttributesKey("EMACSJ_PRIMARY")

internal val EMACSJ_SECONDARY = TextAttributesKey.createTextAttributesKey("EMACSJ_SECONDARY")

internal fun caseSensitive(text: String): Boolean = text.any { isUpperCase(it) && toUpperCase(it) != toLowerCase(it) }

internal fun <T> prependElement(element: T, list: List<T>) =
    (listOf(element) + list).distinct().take(64) // Remove duplicates, keep most recent

internal fun nextPos(oldPos: Int) = maxOf(oldPos - 1, -1)

internal fun previousPos(oldPos: Int, list: List<*>) = minOf(oldPos + 1, list.lastIndex)

internal fun findAllAndHighlight(
    editor: Editor,
    searchArg: String,
    type: SearchType,
    useCase: Boolean,
    range: IntRange? = null,
): List<FindResult> {
    editor.markupModel.removeAllHighlighters()
    val results = mutableListOf<FindResult>()

    if (searchArg.isNotEmpty()) {
        val findManager = FindManager.getInstance(editor.project)
        val findModel = FindModel().apply {
            stringToFind = searchArg
            isCaseSensitive = useCase
            isRegularExpressions = type == REGEXP
        }

        val text = editor.text.substring(0, range?.last ?: editor.text.length)
        var offset = range?.start ?: 0
        while (offset < text.length) {
            val result = findManager.findString(text, offset, findModel)
            if (!result.isStringFound) break
            results.add(result)
            offset = maxOf(result.endOffset, offset + 1) // regexp match can be length zero
        }

        addSecondaryHighlights(editor, results)
    }
    return results
}

private fun addSecondaryHighlights(editor: Editor, matches: List<FindResult>) {
    matches.forEach { match ->
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
