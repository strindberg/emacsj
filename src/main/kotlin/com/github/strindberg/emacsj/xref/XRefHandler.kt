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

internal const val ACTION_XREF_BACK = "com.github.strindberg.emacsj.actions.xref.xrefback"

class XRefHandler : EditorActionHandler() {

    companion object {
        private val places = LimitedStack<PlaceInfo>()

        internal fun pushPlace(event: CommandEvent) {
            event.project?.let { project ->
                FileEditorManagerEx.getInstanceExIfCreated(project)?.let { fileEditorManager ->
                    fileEditorManager.currentFile?.let { virtualFile ->
                        fileEditorManager.getSelectedEditor(virtualFile)?.let { fileEditor ->
                            (fileEditor as? TextEditor)?.editor?.let { editor ->
                                MarkHandler.placeInfo(editor, virtualFile)?.let {
                                    places.push(it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        (editor as? EditorEx)?.let { ex ->
            places.pop()?.let { place ->
                MarkHandler.gotoPlaceInfo(editor, place)
            }
        }
    }
}
