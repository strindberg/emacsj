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

@Suppress("unused")
private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

class ISearchHandler(private val direction: Direction, private val type: SearchType) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            when (current.state) {
                ISearchState.CHOOSE_PREVIOUS -> current.startPreviousSearch()
                ISearchState.SEARCH, ISearchState.FAILED ->
                    if (current.text.isEmpty()) {
                        current.searchAllCarets(direction, getPrevious(current.type))
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

        @Suppress("unused")
        private val companionLogger: Logger = Logger.getInstance("ISearchHandler.companion")

        private var lastStringSearches = listOf<String>()

        private var lastRegexpSearches = listOf<String>()

        private var savedPos = -1

        internal var delegate: ISearchDelegate? = null

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
            savedPos = previousPos(savedPos, list)
            return if (savedPos > -1) list[savedPos] else ""
        }

        internal fun getNext(type: SearchType): String {
            val list = when (type) {
                REGEXP -> lastRegexpSearches
                TEXT -> lastStringSearches
            }
            savedPos = nextPos(savedPos)
            return if (savedPos > -1) list[savedPos] else ""
        }
    }
}
