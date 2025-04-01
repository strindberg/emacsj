package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.preferences.EmacsJSettings
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.jetbrains.annotations.VisibleForTesting

class ISearchHandler(private val direction: Direction, private val type: SearchType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        // Access to ApplicationManager.getApplication() is prohibited during class loading, so we delay until first use of Isearch action.
        if (!initialized) {
            lax = EmacsJSettings.getInstance().state.useLaxISearch
            initialized = true
        }
        val current = delegate
        if (current != null) {
            when (current.state) {
                ISearchState.CHOOSE_PREVIOUS -> current.startPreviousSearch()
                ISearchState.SEARCH, ISearchState.FAILED ->
                    if (current.text.isEmpty()) {
                        if (current.direction == direction) {
                            current.searchAllCarets(direction, getPrevious(current.type), keepStart = true)
                        } else {
                            current.direction = direction
                            current.initTitleText()
                        }
                    } else {
                        current.searchAllCarets(direction, keepStart = false)
                    }
            }
        } else {
            MarkHandler.pushPlaceInfo(editor)
            delegate = ISearchDelegate(editor, type, direction)
        }
    }

    companion object {

        internal var delegate: ISearchDelegate? = null

        @VisibleForTesting
        internal var lastStringSearches = listOf<String>()

        private var lastRegexpSearches = listOf<String>()

        private var savedPos = -1

        internal var lax: Boolean = false

        private var initialized = false

        internal fun swapLax() {
            lax = !lax
        }

        internal fun searchConcluded(text: String, type: SearchType) {
            savedPos = -1
            if (text.isNotEmpty()) {
                when (type) {
                    REGEXP -> lastRegexpSearches = prependElement(text, lastRegexpSearches)
                    TEXT -> lastStringSearches = prependElement(text, lastStringSearches)
                }
            }
        }

        internal fun getPrevious(type: SearchType): String {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = list.previousPos(savedPos)
            return if (savedPos > -1) list[savedPos] else ""
        }

        internal fun getNext(type: SearchType): String {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = list.nextPos(savedPos)
            return if (savedPos > -1) list[savedPos] else ""
        }
    }
}
