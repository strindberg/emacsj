package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.mark.MarkHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_REPLACE_REGEXP = "com.github.strindberg.emacsj.actions.search.replaceregexp"

@Language("devkit-action-id")
internal const val ACTION_REPLACE_TEXT = "com.github.strindberg.emacsj.actions.search.replacetext"

internal class ReplaceHandler(private val type: SearchType) : EditorActionHandler() {

    // Query replace cannot run in a project-less editor.
    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean = editor.project != null

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (delegate == null) {
            editor.project?.let { project ->
                MarkHandler.pushPlaceInfo(editor)
                delegate = ReplaceDelegate(
                    editor = editor,
                    project = project,
                    type = type,
                    selection = with(editor.selectionModel) { if (hasSelection()) selectionStart..selectionEnd else null },
                    lastSearch = getLast(type)
                ).apply {
                    show()
                }
            }
        }
    }

    companion object {

        private val searches = SearchHistory<Replace>()

        internal var delegate: ReplaceDelegate? = null

        internal fun resetPos() {
            searches.rewind()
        }

        internal fun addPrevious(search: String, replacement: String, type: SearchType) {
            searches.add(type, Replace(search, replacement))
        }

        internal fun getPrevious(type: SearchType): Replace = searches.previous(type) ?: Replace.EMPTY

        internal fun getNext(type: SearchType): Replace = searches.next(type) ?: Replace.EMPTY

        internal fun getLast(type: SearchType): Replace? = searches.latest(type)
    }
}
