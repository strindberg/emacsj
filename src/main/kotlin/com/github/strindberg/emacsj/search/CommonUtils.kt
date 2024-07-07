package com.github.strindberg.emacsj.search

import java.lang.Character.isUpperCase
import java.lang.Character.toLowerCase
import java.lang.Character.toUpperCase
import com.intellij.openapi.editor.colors.TextAttributesKey

enum class SearchType { TEXT, REGEXP }

internal val EMACSJ_PRIMARY = TextAttributesKey.createTextAttributesKey("EMACSJ_PRIMARY")

internal val EMACSJ_SECONDARY = TextAttributesKey.createTextAttributesKey("EMACSJ_SECONDARY")

internal fun caseSensitive(text: String): Boolean = text.any { isUpperCase(it) && toUpperCase(it) != toLowerCase(it) }

internal fun <T> prependElement(element: T, list: List<T>) =
    (listOf(element) + list).distinct().take(64) // Remove duplicates, keep most recent

internal fun List<*>.nextPos(oldPos: Int) = maxOf(oldPos - 1, -1)

internal fun List<*>.previousPos(oldPos: Int) = minOf(oldPos + 1, lastIndex)
