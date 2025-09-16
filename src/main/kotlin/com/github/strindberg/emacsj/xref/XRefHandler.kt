package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.github.strindberg.emacsj.mark.UndoStack
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
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

class XRefHandler(private val type: XRefType) : EditorActionHandler() {

    companion object {
        private val places = mutableMapOf<Int, UndoStack<PlaceInfo>>()

        internal fun pushPlace(event: CommandEvent) {
            event.project?.let { project ->
                withCurrentEditorAndFile(project) { editor, file ->
                    pushPlaceInfo(editor, project, file)
                }
            }
        }

        private fun getPlaceForBackAction(editor: Editor, project: Project): PlaceInfo? =
            getPlaceUsingHistory(editor, project) { stack, current -> stack.undo(current) }

        private fun getPlaceForForwardAction(editor: Editor, project: Project): PlaceInfo? =
            getPlaceUsingHistory(editor, project) { stack, current -> stack.redo(current) }

        private fun pushPlace(editor: Editor, project: Project) {
            FileEditorManagerEx.getInstanceExIfCreated(project)?.let { manager ->
                manager.currentFile?.let { virtualFile ->
                    pushPlaceInfo(editor, project, virtualFile)
                }
            }
        }

        private fun withCurrentEditorAndFile(project: Project, block: (Editor, VirtualFile) -> Unit) {
            FileEditorManagerEx.getInstanceExIfCreated(project)?.let { manager ->
                manager.currentFile?.let { virtualFile ->
                    (manager.getSelectedEditor(virtualFile) as? TextEditor)?.let { fileEditor ->
                        block(fileEditor.editor, virtualFile)
                    }
                }
            }
        }

        private fun pushPlaceInfo(editor: Editor, project: Project, virtualFile: VirtualFile) {
            MarkHandler.placeInfo(editor, virtualFile)?.let {
                getStack(project).push(it)
            }
        }

        private fun getPlaceUsingHistory(
            editor: Editor,
            project: Project,
            operation: (UndoStack<PlaceInfo>, PlaceInfo) -> PlaceInfo?,
        ): PlaceInfo? = places[project.hashCode()]?.let { stack ->
            FileEditorManagerEx.getInstanceExIfCreated(project)?.currentFile?.let { currentFile ->
                MarkHandler.placeInfo(editor, currentFile)?.let { currentPlace ->
                    operation(stack, currentPlace)
                }
            }
        }

        private fun getStack(project: Project): UndoStack<PlaceInfo> = places.getOrPut(project.hashCode()) { UndoStack() }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val project = (editor as? EditorEx)?.project ?: return

        when (type) {
            XRefType.BACK -> {
                getPlaceForBackAction(editor, project)?.let { place ->
                    MarkHandler.gotoPlaceInfo(editor, place)
                }
            }

            XRefType.FORWARD -> {
                getPlaceForForwardAction(editor, project)?.let { place ->
                    MarkHandler.gotoPlaceInfo(editor, place)
                }
            }

            XRefType.PUSH -> {
                pushPlace(editor, project)
            }
        }
    }
}
