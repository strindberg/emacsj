package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

internal const val ACTION_REPLACE_REGEXP = "com.github.strindberg.emacsj.actions.search.replaceregexp"
internal const val ACTION_REPLACE_TEXT = "com.github.strindberg.emacsj.actions.search.replacetext"
internal const val ACTION_REPLACE_PREVIOUS = "com.github.strindberg.emacsj.actions.search.replaceprevious"

class ReplaceHandler(private val type: SearchType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (delegate == null) {
            MarkHandler.pushPlaceInfo(editor)
            delegate = ReplaceDelegate(
                editor,
                type,
                with(editor.selectionModel) { if (hasSelection()) selectionStart..selectionEnd else null },
                getLast(type)
            )
        }
    }

    companion object {

        private var lastStringSearches = listOf<Replace>()

        private var lastRegexpSearches = listOf<Replace>()

        private var savedPos = -1

        internal var delegate: ReplaceDelegate? = null

        internal fun resetPos() {
            savedPos = -1
        }

        internal fun addPrevious(search: String, replacement: String, type: SearchType) {
            when (type) {
                REGEXP -> lastRegexpSearches = prependElement(Replace(search, replacement), lastRegexpSearches)
                TEXT -> lastStringSearches = prependElement(Replace(search, replacement), lastStringSearches)
            }
            resetPos()
        }

        internal fun getPrevious(type: SearchType): Replace {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = list.previousPos(savedPos)
            return if (savedPos > -1) list[savedPos] else Replace.EMPTY
        }

        internal fun getNext(type: SearchType): Replace {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = list.nextPos(savedPos)
            return if (savedPos > -1) list[savedPos] else Replace.EMPTY
        }

        internal fun getLast(type: SearchType): Replace? =
            when (type) {
                REGEXP -> if (lastRegexpSearches.isNotEmpty()) lastRegexpSearches[0] else null
                TEXT -> if (lastStringSearches.isNotEmpty()) lastStringSearches[0] else null
            }
    }
}
