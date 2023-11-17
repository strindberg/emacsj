package com.github.strindberg.emacsj.search

import java.lang.invoke.MethodHandles
import com.github.strindberg.emacsj.movement.MarkHandler
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class ReplaceHandler(private val type: SearchType) : EditorActionHandler() {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

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

        @Suppress("unused")
        private val companionLogger: Logger = Logger.getInstance("ReplaceHandler.companion")

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
            savedPos = previousPos(savedPos, list)
            return if (savedPos > -1) list[savedPos] else Replace.EMPTY
        }

        internal fun getNext(type: SearchType): Replace {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = nextPos(savedPos)
            return if (savedPos > -1) list[savedPos] else Replace.EMPTY
        }

        internal fun getLast(type: SearchType): Replace? =
            when (type) {
                REGEXP -> if (lastRegexpSearches.isNotEmpty()) lastRegexpSearches[0] else null
                TEXT -> if (lastStringSearches.isNotEmpty()) lastStringSearches[0] else null
            }
    }
}
