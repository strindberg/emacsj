package com.github.strindberg.emacsj.search

import java.lang.Character.isUpperCase
import java.lang.Character.toLowerCase
import java.lang.Character.toUpperCase
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.TextAttributes

internal val EMACSJ_PRIMARY = TextAttributesKey.createTextAttributesKey("EMACSJ_PRIMARY")

internal val EMACSJ_SECONDARY = TextAttributesKey.createTextAttributesKey("EMACSJ_SECONDARY")

/**
 * Attributes that contribute nothing, used to hide the identifier-under-caret highlight while a search is running.
 */
internal val NO_ATTRIBUTES = TextAttributes()

internal fun caseSensitive(text: String): Boolean = text.any { isUpperCase(it) && toUpperCase(it) != toLowerCase(it) }

// Sticky selection must be toggled off first to allow new start position.
internal fun EditorEx.startStickySelection() {
    isStickySelection = false
    isStickySelection = true
}

private const val DEFAULT_LIMIT = 64

/**
 * A most-recently-used history: [push] moves an element to the front, deduplicates, and the size never grows past [limit].
 */
internal class History<T>(private val limit: Int = DEFAULT_LIMIT) {

    private val elements = ArrayList<T>()

    val size: Int get() = elements.size

    fun getOrNull(index: Int): T? = elements.getOrNull(index)

    // This method is not a traditional stack push since it deduplicates already pushed elements.
    fun push(element: T) {
        val updated = (listOf(element) + elements).distinct().take(limit)
        elements.clear()
        elements.addAll(updated)
    }

    fun pop(): T? = elements.removeFirstOrNull()

    fun peek(): T? = elements.firstOrNull()

    fun clear() {
        elements.clear()
    }
}

enum class SearchType { TEXT, REGEXP }
