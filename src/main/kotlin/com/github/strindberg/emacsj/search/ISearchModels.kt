package com.github.strindberg.emacsj.search

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.util.Key

private val CARET_SEARCH_DATA_KEY = Key.create<CaretSearch>("ISearchHandler.CARET_SEARCH_DATA_KEY")
private val CARET_BREADCRUMBS_KEY = Key.create<MutableList<CaretBreadcrumb>>("ISearchHandler.CARET_BREADCRUMBS_KEY")

internal var Caret.search: CaretSearch
    get() = getUserData(CARET_SEARCH_DATA_KEY) ?: CaretSearch(offset)
    set(searchData) {
        putUserData(CARET_SEARCH_DATA_KEY, searchData)
    }

internal var Caret.breadcrumbs: MutableList<CaretBreadcrumb>
    get() = getUserData(CARET_BREADCRUMBS_KEY) ?: mutableListOf()
    set(breadcrumbs) {
        putUserData(CARET_BREADCRUMBS_KEY, breadcrumbs)
    }

internal data class CaretSearch(val start: Int, val searchStart: Int = start, val match: Match = Match(start, start))

internal data class Match(val start: Int, val end: Int)

internal data class CaretBreadcrumb(val caretSearch: CaretSearch, val direction: Direction) {
    val searchStart by caretSearch::searchStart
    val match by caretSearch::match
}

internal data class EditorBreadcrumb(val title: String, val text: String, val state: ISearchState, val count: Pair<Int, Int>?)

internal enum class ISearchState { SEARCH, FAILED, CHOOSE_PREVIOUS }

internal data class SearchResult(val found: Boolean, val offset: Int?, val wrapped: Boolean)

enum class Direction { FORWARD, BACKWARD }
