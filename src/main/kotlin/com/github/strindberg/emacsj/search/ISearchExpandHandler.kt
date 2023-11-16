package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.search.Type.CHARACTER
import com.github.strindberg.emacsj.search.Type.LINE
import com.github.strindberg.emacsj.search.Type.NEW_LINE
import com.github.strindberg.emacsj.search.Type.WORD
import com.github.strindberg.emacsj.word.currentWordEnd
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.util.DocumentUtil

enum class Type { WORD, LINE, CHARACTER, NEW_LINE }

class ISearchExpandHandler(val type: Type) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.state == ISearchState.SEARCH }?.let { delegate ->
            val newText = when (type) {
                WORD -> getWord(editor, editor.caretModel.currentCaret.search.match.end)
                LINE -> getLine(editor, editor.caretModel.currentCaret.search.match.end)
                CHARACTER -> getChar(editor, editor.caretModel.currentCaret.search.match.end)
                NEW_LINE -> "\n"
            }
            delegate.searchAllCarets(delegate.direction, newText, keepStart = false)
        }
    }

    private fun getWord(editor: Editor, offset: Int): String =
        editor.document.substring(offset, currentWordEnd(editor, offset) ?: (editor.text.lastIndex))

    private fun getLine(editor: Editor, offset: Int): String =
        editor.document.substring(offset, DocumentUtil.getLineEndOffset(offset, editor.document))

    private fun getChar(editor: Editor, offset: Int): String =
        editor.document.substring(offset, minOf(offset + 1, editor.document.textLength))
}
