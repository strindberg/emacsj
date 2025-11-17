package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.Type.POP
import com.github.strindberg.emacsj.search.prependElement
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Language

enum class Type { PUSH, POP }

@Language("devkit-action-id")
internal const val ACTION_PUSH_MARK = "com.github.strindberg.emacsj.actions.mark.pushmark"

@Language("devkit-action-id")
internal const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"

class MarkHandler(val type: Type) : EditorActionHandler() {

    companion object {
        private val places = mutableMapOf<Int, LimitedStack<PlaceInfo>>()

        internal fun pushPlaceInfo(editor: Editor) {
            (editor as? EditorEx)?.let {
                editor.virtualFile?.let { virtualFile ->
                    placeInfo(editor, virtualFile)?.let { placeInfo ->
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

        internal fun placeInfo(editor: Editor, virtualFile: VirtualFile): PlaceInfo? =
            editor.project?.manager?.getSelectedEditorWithProvider(virtualFile)?.let { editorWithProvider ->
                PlaceInfo(
                    virtualFile,
                    editorWithProvider.fileEditor.getState(FileEditorStateLevel.UNDO),
                    editorWithProvider.provider.editorTypeId,
                    editor.caretModel.primaryCaret.offset,
                    editor.scrollingModel.verticalScrollOffset,
                )
            }

        internal fun gotoPlaceInfo(editor: EditorEx, info: PlaceInfo) {
            editor.project?.manager?.let { manager ->
                manager.openFile(info.file, focusEditor = true)
                manager.setSelectedEditor(info.file, info.editorTypeId)
                manager.getSelectedEditorWithProvider(info.file)?.takeIf {
                    it.provider.editorTypeId == info.editorTypeId
                }?.let {
                    it.fileEditor.setState(info.state)
                    editor.scrollingModel.scrollVertically(info.scrollOffset)
                }
            }
        }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        (editor as? EditorEx)?.let {
            editor.virtualFile?.let { virtualFile ->
                if (type == POP || EmacsJService.instance.isLastStrictUniversal()) {
                    places[virtualFile.hashCode()]?.pop()?.let { place ->
                        gotoPlaceInfo(editor, place)
                    }
                } else {
                    val previousSticky = editor.isStickySelection
                    editor.isStickySelection = false
                    placeInfo(editor, virtualFile)?.let { placeInfo ->
                        if (placeInfo != peek(editor) || !previousSticky) {
                            places.getOrPut(virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                            editor.isStickySelection = true
                        }
                    }
                }
            }
        }
    }
}

internal val Project.manager: FileEditorManagerEx?
    get() = FileEditorManagerEx.getInstanceExIfCreated(this)

class PlaceInfo(
    val file: VirtualFile,
    val state: FileEditorState,
    val editorTypeId: String,
    val caretPosition: Int,
    val scrollOffset: Int,
) {
    override fun equals(other: Any?): Boolean =
        (other as? PlaceInfo)?.let {
            file == other.file && caretPosition == other.caretPosition
        } == true

    override fun hashCode(): Int = 31 * file.hashCode() + caretPosition.hashCode()
}

class UndoStack<T> {
    private var undoStack = listOf<T>()
    private var redoStack = listOf<T>()

    /**
     * Push a new position onto the undo stack and clear the redo stack.
     */
    fun push(position: T) {
        undoStack = prependElement(position, undoStack)
        redoStack = listOf()
    }

    /**
     * Undo the last cursor movement.
     * Stores the current position in the redo stack,
     * and returns the previous position from the undo stack.
     */
    fun undo(current: T): T? {
        if (undoStack.isEmpty()) return null

        val previous = undoStack.first()
        undoStack = undoStack.drop(1)

        redoStack = prependElement(current, redoStack)

        return previous
    }

    /**
     * Redo the last undone cursor movement.
     * Stores the current position in the undo stack,
     * and returns the redone position from the redo stack.
     */
    fun redo(current: T): T? {
        if (redoStack.isEmpty()) return null

        val next = redoStack.first()
        redoStack = redoStack.drop(1)

        undoStack = prependElement(current, undoStack)

        return next
    }
}

class LimitedStack<T> {

    private var elements = listOf<T>()

    fun push(element: T) {
        elements = prependElement(element, elements)
    }

    fun pop(): T? = elements.firstOrNull()?.apply { elements = elements.drop(1) }

    fun peek(): T? = elements.firstOrNull()
}
