package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.paste.clipboardHistoryTexts

internal class ISearchKillRingUtil {

    private var history: List<String> = emptyList()

    private var position = 0

    /** What the latest paste added, empty once the walk has been invalidated. */
    private var pasted: String = ""

    fun start(clipboard: String) {
        history = clipboardHistoryTexts()
        position = 0
        pasted = clipboard
    }

    /** The next step of the walk, or null when there is no live walk to continue. */
    fun next(): Swap? {
        if (history.isEmpty() || pasted.isEmpty()) {
            return null
        }

        val replaced = pasted.length
        position = (position + 1) % history.size
        pasted = history[position]

        return Swap(replaced, pasted)
    }

    fun invalidate() {
        pasted = ""
    }

    /** Take [replacedLength] characters off the end of the search string, then add [inserted]. */
    data class Swap(val replacedLength: Int, val inserted: String)
}
