package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.preferences.EmacsJSettings
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

internal class ISearchHandler(private val direction: SearchDirection, private val type: SearchType) : EditorActionHandler() {

    // Incremental search cannot run in a project-less editor.
    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean = editor.project != null

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (current.isActive()) {
                if (current.text.isEmpty()) {
                    if (current.direction == direction) {
                        current.search(searchDirection = direction, newText = getPrevious(current.searchType))
                    } else {
                        current.direction = direction
                        current.initTitleText()
                    }
                } else {
                    current.search(searchDirection = direction)
                }
            } else {
                current.startEditedSearch()
            }
        } else {
            editor.project?.let { project ->
                MarkHandler.pushPlaceInfo(editor)
                delegate = ISearchDelegate(editor = editor, project = project, searchType = type, direction = direction)
            }
        }
    }

    companion object {

        internal var delegate: ISearchDelegate? = null

        @VisibleForTesting
        internal val searches = SearchHistory<String>()

        private var isLaxInitialized = false

        private var isSelectionSearchInitialized = false

        internal var isLax: Boolean = false
            get() {
                if (!isLaxInitialized) {
                    field = EmacsJSettings.getInstance().state.useLaxISearch // We can't access this value in constructor
                    isLaxInitialized = true
                }
                return field
            }
            set(value) {
                field = value
                isLaxInitialized = true
            }

        internal fun toggleLax() {
            isLax = !isLax
        }

        internal var isSelectionISearch: Boolean = false
            get() {
                if (!isSelectionSearchInitialized) {
                    field = EmacsJSettings.getInstance().state.useSelectionISearch // We can't access this value in constructor
                    isSelectionSearchInitialized = true
                }
                return field
            }
            set(value) {
                field = value
                isSelectionSearchInitialized = true
            }

        internal fun searchConcluded(text: String, type: SearchType) {
            if (text.isEmpty()) searches.rewind() else searches.add(type, text)
        }

        internal fun getPrevious(type: SearchType): String = searches.previous(type).orEmpty()

        internal fun getNext(type: SearchType): String = searches.next(type).orEmpty()
    }
}
