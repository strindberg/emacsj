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

/**
 * A stack designed for undo/redo type functionality.
 * Undo and Redo operations should not modify the elements in the stack.
 * Push should drop all elements that could have been Redo-ed before the push.
 * */
class UndoStack<T> {

    private var elements = listOf<T>()
    // 0 when things are being pushed with no BACK actions performed
    // >0 when a BACK action has just been performed, etc
    private var currIndex = 0

    /**
     * prepends a new item to the top of the stack.
     * drops all items that could have been Redo-ed before the push
     * */
    fun push(element: T) {

        // drop everything before current index
        // this is like dropping a bunch of redo elements after
        // undo-ing a lot and then performing an action
        val droppedList = elements.drop(currIndex)

        elements = prependElement(element, droppedList)
        // reset to 0 since we dropped the old elements
        currIndex = 0
    }

    fun isEmpty(): Boolean {
        return elements.isEmpty()
    }

    /**
     *
     */
    fun redo(): T? {
        return null
    }

    /**
     * move -> in the stack without modifying it.
     * If stack is empty, no-op
     */
    fun undo(): T? {
        if (isEmpty()) return null

        val item = elements[currIndex]
        currIndex++
        return item
    }

    /**
     * move -> in the stack, but if we are at the top of the stack, save the passed
     * in element as the new first element
     * */
    fun undoSaveFirst(element: T): T? {
        if (isEmpty()) return null

        val currentItem = elements[currIndex]

        if (currIndex == 0) {

            elements = prependElement(element, elements)

            currIndex = 2 // since we are moving 1 AND adding 1 element
        } else {
            currIndex++
        }

        return currentItem
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
