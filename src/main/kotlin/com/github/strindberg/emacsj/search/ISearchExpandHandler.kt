package com.github.strindberg.emacsj.search

import com.github.strindberg.emacsj.search.Type.CHARACTER
import com.github.strindberg.emacsj.search.Type.LINE
import com.github.strindberg.emacsj.search.Type.NEW_LINE
import com.github.strindberg.emacsj.search.Type.WORD
import com.github.strindberg.emacsj.word.currentWordEnd
import com.github.strindberg.emacsj.word.isCamel
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.word.text
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.util.DocumentUtil
import org.intellij.lang.annotations.Language

enum class Type { WORD, LINE, CHARACTER, NEW_LINE }

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_WORD = "com.github.strindberg.emacsj.actions.search.isearchword"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_LINE = "com.github.strindberg.emacsj.actions.search.isearchline"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_CHAR = "com.github.strindberg.emacsj.actions.search.isearchchar"

@Language("devkit-action-id")
internal const val ACTION_ISEARCH_NEWLINE = "com.github.strindberg.emacsj.actions.search.isearchnewline"

class ISearchExpandHandler(val type: Type) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        ISearchHandler.delegate?.takeIf { it.isActive() }?.let { delegate ->
            val newText = when (type) {
                WORD -> getWord(editor, editor.caretModel.currentCaret.search.match.end)
                LINE -> getLine(editor, editor.caretModel.currentCaret.search.match.end)
                CHARACTER -> getChar(editor, editor.caretModel.currentCaret.search.match.end)
                NEW_LINE -> "\n"
            }
            delegate.searchAllCarets(searchDirection = delegate.direction, newText = newText, keepStart = false)
        }
    }

    private fun getWord(editor: Editor, offset: Int): String =
        editor.document.substring(offset, currentWordEnd(editor.text, offset, editor.isCamel))

    private fun getLine(editor: Editor, offset: Int): String =
        editor.document.substring(offset, DocumentUtil.getLineEndOffset(offset, editor.document))

    private fun getChar(editor: Editor, offset: Int): String =
        editor.document.substring(offset, minOf(offset + 1, editor.document.textLength))
}
