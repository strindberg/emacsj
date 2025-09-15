package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.mark.Type.POP
import com.github.strindberg.emacsj.search.prependElement
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
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
            if (editor is EditorEx) {
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
            editor.project?.let { project ->
                FileEditorManagerEx.getInstanceExIfCreated(project)?.let { fileEditorManager ->
                    fileEditorManager.getSelectedEditorWithProvider(virtualFile)?.let { editorWithProvider ->
                        PlaceInfo(
                            virtualFile,
                            editorWithProvider.fileEditor.getState(FileEditorStateLevel.UNDO),
                            editorWithProvider.provider.editorTypeId,
                            editor.caretModel.primaryCaret.offset,
                        )
                    }
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
                if (type == POP || EmacsJCommandListener.lastCommandName == EmacsJBundle.actionText(ACTION_UNIVERSAL_ARGUMENT)) {
                    places[virtualFile.hashCode()]?.pop()?.let { place ->
                        gotoPlaceInfo(editor, place)
                    }
                } else {
                    val previousSticky = ex.isStickySelection
                    ex.isStickySelection = false
                    placeInfo(ex, virtualFile)?.let { placeInfo ->
                        if (placeInfo != peek(ex) || !previousSticky) {
                            places.getOrPut(virtualFile.hashCode()) { LimitedStack() }.push(placeInfo)
                            ex.isStickySelection = true
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
        redoStack = listOf<T>()
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
