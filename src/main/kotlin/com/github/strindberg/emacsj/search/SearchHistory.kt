package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT

/**
 * The plain-text and regexp search histories of one feature, together with the cursor that walks them.
 *
 * A walk belongs to one history: changing the search type part-way through starts again at the most recent entry of
 * the other one, rather than carrying a position that was only ever meaningful in the history it came from.
 *
 * [previous] and [next] return null once the walk steps off the end of a history, leaving it to the caller to say
 * what "no entry" means for its own element type.
 */

private const val NO_ENTRY = -1

internal class SearchHistory<T> {

    private val textHistory = History<T>()

    private val regexpHistory = History<T>()

    private var cursor = NO_ENTRY

    private var walkedType: SearchType? = null

    fun add(type: SearchType, entry: T) {
        history(type).push(entry)
        rewind()
    }

    fun latest(type: SearchType): T? = history(type).peek()

    fun previous(type: SearchType): T? = step(type, 1)

    fun next(type: SearchType): T? = step(type, -1)

    fun rewind() {
        cursor = NO_ENTRY
        walkedType = null
    }

    fun clear(type: SearchType) {
        history(type).clear()
        rewind()
    }

    private fun history(type: SearchType) =
        when (type) {
            TEXT -> textHistory
            REGEXP -> regexpHistory
        }

    private fun step(type: SearchType, delta: Int): T? =
        history(type).let { history ->
            if (type != walkedType) {
                rewind()
                walkedType = type
            }
            cursor = (cursor + delta).coerceIn(NO_ENTRY, history.size - 1)
            history.getOrNull(cursor)
        }
}
