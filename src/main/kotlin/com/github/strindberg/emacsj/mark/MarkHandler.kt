package com.github.strindberg.emacsj.mark

import java.time.Duration
import java.time.LocalDateTime
import com.github.strindberg.emacsj.mark.Type.POP
import com.github.strindberg.emacsj.mark.Type.PUSH
import com.github.strindberg.emacsj.search.prependElement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl.PlaceInfo
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider

enum class Type { PUSH, POP }

const val TIMEOUT = 500L

class MarkHandler(val type: Type) : EditorActionHandler() {

    companion object {
        private val places = mutableMapOf<Int, LimitedStack<PlaceInfoWrapper>>()
        private var lastPush: LocalDateTime = LocalDateTime.now()

        // Used for testing
        internal var editorTypeId: String? = null

        internal fun pushPlaceInfo(editor: Editor) {
            if (editor is EditorEx) {
                placeInfo(editor, editor.caretModel.primaryCaret.offset)?.let { placeInfo ->
                    places.getOrPut(editor.virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                }
            }
        }

        internal fun peek(editor: Editor): PlaceInfoWrapper? =
            if (editor is EditorEx) places[editor.virtualFile.hashCode()]?.peek() else null

        private fun placeInfo(editor: EditorEx, offset: Int): PlaceInfoWrapper? =
            editor.project?.let { project ->
                FileEditorManagerEx.getInstance(project).getSelectedEditor(editor.virtualFile)?.let { fileEditor ->
                    PlaceInfoWrapper(
                        PlaceInfo(
                            editor.virtualFile,
                            fileEditor.getState(FileEditorStateLevel.UNDO),
                            editorTypeId ?: TextEditorProvider.getInstance().editorTypeId,
                            null,
                            editor.document.createRangeMarker(offset, offset)
                        )
                    )
                }
            }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val ex = editor as EditorEx

        when (type) {
            PUSH -> {
                ex.isStickySelection = false
                placeInfo(ex, editor.caretModel.primaryCaret.offset)?.let { placeInfo ->
                    val lastPlaceInfo = peek(editor)
                    if (lastPlaceInfo == null || lastPlaceInfo != placeInfo) {
                        places.getOrPut(editor.virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                        lastPush = LocalDateTime.now()
                        ex.isStickySelection = true
                    } else if (Duration.between(lastPush, LocalDateTime.now()) > Duration.ofMillis(TIMEOUT)) {
                        ex.isStickySelection = true
                    }
                }
            }
            POP -> {
                places[editor.virtualFile.hashCode()]?.pop()?.let { place ->
                    IdeDocumentHistory.getInstance(editor.project).gotoPlaceInfo(place.placeInfo)
                }
            }
        }
    }
}

class PlaceInfoWrapper(val placeInfo: PlaceInfo) {
    override fun equals(other: Any?): Boolean =
        (other as? PlaceInfoWrapper)?.let {
            placeInfo.file == other.placeInfo.file &&
                placeInfo.caretPosition?.startOffset == other.placeInfo.caretPosition?.startOffset
        } ?: false

    override fun hashCode(): Int = 31 * placeInfo.file.hashCode() + placeInfo.caretPosition?.startOffset.hashCode()
}

class LimitedStack<T> {

    private var elements = listOf<T>()

    fun push(element: T) {
        elements = prependElement(element, elements)
    }

    fun pop(): T? = elements.firstOrNull()?.apply { elements = elements.drop(1) }

    fun peek(): T? = elements.firstOrNull()
}
