package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.mark.Type.POP
import com.github.strindberg.emacsj.mark.Type.PUSH
import com.github.strindberg.emacsj.search.prependElement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.vfs.VirtualFile

enum class Type { PUSH, POP }

class MarkHandler(val type: Type) : EditorActionHandler() {

    companion object {
        private val places = mutableMapOf<Int, LimitedStack<PlaceInfo>>()

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

        internal fun peek(editor: Editor): PlaceInfo? =
            (editor as? EditorEx)?.let {
                editor.virtualFile?.let { virtualFile ->
                    places[virtualFile.hashCode()]?.peek()
                }
            }

        private fun placeInfo(editor: EditorEx, offset: Int, virtualFile: VirtualFile): PlaceInfo? =
            editor.project?.let { project ->
                FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(virtualFile)?.let { fileEditor ->
                    PlaceInfo(
                        virtualFile,
                        fileEditor.getState(FileEditorStateLevel.UNDO),
                        editorTypeId ?: TextEditorProvider.getInstance().editorTypeId,
                        offset,
                    )
                }
            }

        internal fun gotoPlaceInfo(editor: EditorEx, info: PlaceInfo) {
            editor.project?.let { project ->
                FileEditorManagerEx.getInstanceExIfCreated(project)?.let { fileEditorManager ->
                    fileEditorManager.openFile(info.file, focusEditor = true)
                    fileEditorManager.setSelectedEditor(info.file, info.editorTypeId)
                    fileEditorManager.getSelectedEditorWithProvider(info.file)?.takeIf {
                        it.provider.editorTypeId == info.editorTypeId
                    }?.fileEditor?.setState(info.state)
                }
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
                            gotoPlaceInfo(editor, place)
                        }
                    }
                }
            }
        }
    }
}

class PlaceInfo(val file: VirtualFile, val state: FileEditorState, val editorTypeId: String, val caretPosition: Int) {
    override fun equals(other: Any?): Boolean =
        (other as? PlaceInfo)?.let {
            file == other.file && caretPosition == other.caretPosition
        } ?: false

    override fun hashCode(): Int = 31 * file.hashCode() + caretPosition.hashCode()
}

class LimitedStack<T> {

    private var elements = listOf<T>()

    fun push(element: T) {
        elements = prependElement(element, elements)
    }

    fun pop(): T? = elements.firstOrNull()?.apply { elements = elements.drop(1) }

    fun peek(): T? = elements.firstOrNull()
}
