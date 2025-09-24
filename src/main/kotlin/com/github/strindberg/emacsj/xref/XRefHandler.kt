package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.github.strindberg.emacsj.mark.UndoStack
import com.github.strindberg.emacsj.mark.manager
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.CommandEvent
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

class XRefHandler(private val type: XRefType) : EditorActionHandler() {

    companion object {
        internal val xRefCommandNames = setOf(
            ActionsBundle.actionText("GotoDeclaration"),
            ActionsBundle.actionText("GotoDeclarationOnly"),
            ActionsBundle.actionText("GotoTypeDeclaration"),
        )

        private val places = mutableMapOf<Int, UndoStack<PlaceInfo>>()

        internal fun pushPlace(event: CommandEvent) {
            event.project?.let { project ->
                project.manager?.let { manager ->
                    manager.currentFile?.let { virtualFile ->
                        (manager.getSelectedEditor(virtualFile) as? TextEditor)?.let { fileEditor ->
                            pushPlaceInfo(fileEditor.editor, project, virtualFile)
                        }
                    }
                }
            }
        }

        private fun getPlaceForBackAction(editor: Editor): PlaceInfo? =
            getPlaceUsingHistory(editor) { stack, current -> stack.undo(current) }

        private fun getPlaceForForwardAction(editor: Editor): PlaceInfo? =
            getPlaceUsingHistory(editor) { stack, current -> stack.redo(current) }

        private fun pushPlace(editor: EditorEx) {
            editor.project?.let { project ->
                pushPlaceInfo(editor, project, editor.virtualFile)
            }
        }

        private fun pushPlaceInfo(editor: Editor, project: Project, virtualFile: VirtualFile) {
            MarkHandler.placeInfo(editor, virtualFile)?.let {
                places.getOrPut(project.hashCode()) { UndoStack() }.push(it)
            }
        }

        private fun getPlaceUsingHistory(
            editor: Editor,
            operation: (UndoStack<PlaceInfo>, PlaceInfo) -> PlaceInfo?,
        ): PlaceInfo? =
            editor.project?.let { project ->
                places[project.hashCode()]?.let { stack ->
                    editor.virtualFile?.let { currentFile ->
                        MarkHandler.placeInfo(editor, currentFile)?.let { currentPlace ->
                            operation(stack, currentPlace)
                        }
                    }
                }
            }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        (editor as? EditorEx)?.let {
            when (type) {
                XRefType.BACK -> getPlaceForBackAction(editor)?.let { place ->
                    MarkHandler.gotoPlaceInfo(editor, place)
                }
                XRefType.FORWARD -> getPlaceForForwardAction(editor)?.let { place ->
                    MarkHandler.gotoPlaceInfo(editor, place)
                }
                XRefType.PUSH -> pushPlace(editor)
            }
        }
    }
}
