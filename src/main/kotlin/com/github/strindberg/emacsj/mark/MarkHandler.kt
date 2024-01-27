package com.github.strindberg.emacsj.mark

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
import com.intellij.openapi.vfs.VirtualFile

enum class Type { PUSH, POP }

class MarkHandler(val type: Type) : EditorActionHandler() {

    companion object {
        private val places = mutableMapOf<Int, LimitedStack<PlaceInfoWrapper>>()

        // Used for testing
        internal var editorTypeId: String? = null

        internal fun pushPlaceInfo(editor: Editor) {
            if (editor is EditorEx) {
                editor.virtualFile?.let { virtualFile ->
                    placeInfo(editor, editor.caretModel.primaryCaret.offset, virtualFile)?.let { placeInfo ->
                        places.getOrPut(virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                    }
                }
            }
        }

        internal fun peek(editor: Editor): PlaceInfoWrapper? =
            (editor as? EditorEx)?.let {
                editor.virtualFile?.let { virtualFile ->
                    places[virtualFile.hashCode()]?.peek()
                }
            }

        private fun placeInfo(editor: EditorEx, offset: Int, virtualFile: VirtualFile): PlaceInfoWrapper? =
            editor.project?.let { project ->
                FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(virtualFile)?.let { fileEditor ->
                    PlaceInfoWrapper(
                        PlaceInfo(
                            virtualFile,
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
        (editor as? EditorEx)?.let { ex ->
            ex.virtualFile?.let { virtualFile ->
                when (type) {
                    PUSH -> {
                        val previousSticky = ex.isStickySelection
                        ex.isStickySelection = false
                        placeInfo(ex, ex.caretModel.primaryCaret.offset, virtualFile)?.let { placeInfo ->
                            if (placeInfo != peek(ex) || !previousSticky) {
                                places.getOrPut(virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                                ex.isStickySelection = true
                            }
                        }
                    }
                    POP -> {
                        places[virtualFile.hashCode()]?.pop()?.let { place ->
                            IdeDocumentHistory.getInstance(ex.project).gotoPlaceInfo(place.placeInfo)
                        }
                    }
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
