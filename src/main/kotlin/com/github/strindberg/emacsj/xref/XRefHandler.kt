package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.MarkHandler.Companion.placeInfo
import com.github.strindberg.emacsj.mark.MarkHandler.Companion.restore
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.github.strindberg.emacsj.mark.manager
import com.github.strindberg.emacsj.search.History
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Language

enum class XRefType { BACK, PUSH, FORWARD }

@Language("devkit-action-id")
internal const val ACTION_XREF_BACK = "com.github.strindberg.emacsj.actions.xref.xrefback"

@Language("devkit-action-id")
internal const val ACTION_XREF_PUSH = "com.github.strindberg.emacsj.actions.xref.xrefpush"

@Language("devkit-action-id")
internal const val ACTION_XREF_FORWARD = "com.github.strindberg.emacsj.actions.xref.xrefforward"

internal class XRefHandler(private val type: XRefType) : EditorActionHandler() {

    companion object {

        internal val xRefActionIds = setOf(
            "GotoDeclaration",
            "GotoDeclarationOnly",
            "GotoTypeDeclaration",
        )

        internal fun Project.pushPlace() {
            manager?.let { manager ->
                manager.selectedFiles.getOrNull(0)?.let { virtualFile ->
                    (manager.getSelectedEditor(virtualFile) as? TextEditor)?.run { editor.pushPlaceInfo(this@pushPlace, virtualFile) }
                }
            }
        }

        private fun Editor.getPlaceForBackAction(): PlaceInfo? =
            getPlaceUsingHistory { current -> undo(current) }

        private fun Editor.getPlaceForForwardAction(): PlaceInfo? =
            getPlaceUsingHistory { current -> redo(current) }

        private fun Editor.pushPlace() {
            project?.let { project ->
                pushPlaceInfo(project, virtualFile)
            }
        }

        private fun Editor.pushPlaceInfo(project: Project, virtualFile: VirtualFile) {
            virtualFile.placeInfo(this)?.let { placeInfo ->
                project.xrefStack().push(placeInfo)
            }
        }

        private fun Editor.getPlaceUsingHistory(operation: UndoRedoStack<PlaceInfo>.(PlaceInfo) -> PlaceInfo?): PlaceInfo? =
            virtualFile?.placeInfo(this)?.let { currentPlace ->
                project?.run { xrefStack().operation(currentPlace) }
            }

        private fun Project.xrefStack() = service<XRefPlaces>().stack
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (editor is EditorEx) {
            when (type) {
                XRefType.BACK -> editor.getPlaceForBackAction()?.restore(editor)
                XRefType.FORWARD -> editor.getPlaceForForwardAction()?.restore(editor)
                XRefType.PUSH -> editor.pushPlace()
            }
        }
    }
}

class UndoRedoStack<T> {

    private val undoStack = History<T>()
    private val redoStack = History<T>()

    /**
     * Records a new position, which discards anything that was redoable.
     */
    fun push(position: T) {
        undoStack.push(position)
        redoStack.clear()
    }

    /**
     * Steps back one position: [current] becomes redoable, and the position before it is returned.
     */
    fun undo(current: T): T? = undoStack.pop()?.also { redoStack.push(current) }

    /**
     * Steps forward one position: [current] becomes undoable, and the position after it is returned.
     */
    fun redo(current: T): T? = redoStack.pop()?.also { undoStack.push(current) }
}
