package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.MarkHandler.Companion.placeInfo
import com.github.strindberg.emacsj.mark.MarkHandler.Companion.restore
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.github.strindberg.emacsj.mark.UndoRedoStack
import com.github.strindberg.emacsj.mark.manager
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

        internal fun pushPlace(project: Project) {
            project.manager?.let { manager ->
                manager.selectedFiles.getOrNull(0)?.let { virtualFile ->
                    (manager.getSelectedEditor(virtualFile) as? TextEditor)?.let { fileEditor ->
                        pushPlaceInfo(fileEditor.editor, project, virtualFile)
                    }
                }
            }
        }

        private fun getPlaceForBackAction(editor: Editor): PlaceInfo? =
            getPlaceUsingHistory(editor) { current -> undo(current) }

        private fun getPlaceForForwardAction(editor: Editor): PlaceInfo? =
            getPlaceUsingHistory(editor) { current -> redo(current) }

        private fun pushPlace(editor: EditorEx) {
            editor.project?.let { project ->
                pushPlaceInfo(editor, project, editor.virtualFile)
            }
        }

        private fun pushPlaceInfo(editor: Editor, project: Project, virtualFile: VirtualFile) {
            virtualFile.placeInfo(editor)?.let { placeInfo ->
                project.xrefStack().push(placeInfo)
            }
        }

        private fun getPlaceUsingHistory(editor: Editor, operation: UndoRedoStack<PlaceInfo>.(PlaceInfo) -> PlaceInfo?): PlaceInfo? =
            editor.virtualFile?.placeInfo(editor)?.let { currentPlace ->
                editor.project?.run { xrefStack().operation(currentPlace) }
            }

        private fun Project.xrefStack() = service<XRefPlaces>().stack
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (editor is EditorEx) {
            when (type) {
                XRefType.BACK -> getPlaceForBackAction(editor)?.restore(editor)
                XRefType.FORWARD -> getPlaceForForwardAction(editor)?.restore(editor)
                XRefType.PUSH -> pushPlace(editor)
            }
        }
    }
}
