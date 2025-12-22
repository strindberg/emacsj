package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.preferences.EmacsJSettings
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_BACKWARD = "com.github.strindberg.emacsj.actions.search.isearchtextbackward"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_FORWARD = "com.github.strindberg.emacsj.actions.search.isearchtextforward"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_REGEXP_FORWARD = "com.github.strindberg.emacsj.actions.search.isearchregexpforward"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_REGEXP_BACKWARD = "com.github.strindberg.emacsj.actions.search.isearchregexpbackward"

class ISearchHandler(private val direction: Direction, private val type: SearchType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (current.isActive()) {
                if (current.text.isEmpty()) {
                    if (current.direction == direction) {
                        current.searchAllCarets(searchDirection = direction, newText = getPrevious(current.type))
                    } else {
                        current.direction = direction
                        current.initTitleText()
                    }
                } else {
                    current.searchAllCarets(searchDirection = direction, newText = "")
                }
            } else {
                current.startEditedSearch()
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

        @VisibleForTesting
        internal var lastRegexpSearches = listOf<String>()

        private var savedPos = -1

        private var initialized = false

        internal var lax: Boolean = false
            get() {
                if (!initialized) {
                    field = EmacsJSettings.getInstance().state.useLaxISearch // We can't access this value in constructor
                    initialized = true
                }
                return field
            }

        internal fun toggleLax() {
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
