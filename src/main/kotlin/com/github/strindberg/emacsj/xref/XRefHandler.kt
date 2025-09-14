package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.LimitedStack
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.PlaceInfo
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

class XRefHandler(val type: XRefType) : EditorActionHandler() {

    companion object {
        internal val places = mutableMapOf<Int, LimitedStack<PlaceInfo>>()

        private fun pushPlaceInfo(editor: Editor, project: Project, virtualFile: VirtualFile) {
            MarkHandler.placeInfo(editor, virtualFile)?.let {
                places.getOrPut(project.hashCode()) { LimitedStack() }.push(it)
            }
        }

        internal fun pushPlace(editor: Editor, project: Project) {
            FileEditorManagerEx.getInstanceExIfCreated(project)?.let { fileEditorManager ->
                fileEditorManager.currentFile?.let { virtualFile ->
                    pushPlaceInfo(editor, project, virtualFile)
                }
            }
        }

        internal fun pushPlace(event: CommandEvent) {
            event.project?.let { project ->
                FileEditorManagerEx.getInstanceExIfCreated(project)?.let { fileEditorManager ->
                    fileEditorManager.currentFile?.let { virtualFile ->
                        fileEditorManager.getSelectedEditor(virtualFile)?.let { fileEditor ->
                            (fileEditor as? TextEditor)?.editor?.let { editor ->
                                pushPlaceInfo(editor, project, virtualFile)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        when (type) {
            XRefType.BACK -> {
                (editor as? EditorEx)?.let { ex ->
                    places[ex.project.hashCode()]?.pop()?.let { place ->
                        MarkHandler.gotoPlaceInfo(editor, place)
                    }
                }
            }
            XRefType.PUSH -> {
                (editor as? EditorEx)?.project?.let { project ->
                    pushPlace(editor, project)
                }
            }
            XRefType.FORWARD -> {
                // noop
            }
        }
    }
}
