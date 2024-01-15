package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.LimitedStack
import com.github.strindberg.emacsj.mark.PlaceInfoWrapper
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl.PlaceInfo
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider

class XRefCommandListener : CommandListener {

    companion object {
        private val places = LimitedStack<PlaceInfoWrapper>()

        // Used for testing
        internal var editorTypeId: String? = null

        fun pop(): PlaceInfoWrapper? = places.pop()
    }

    override fun commandStarted(event: CommandEvent) {
        if (event.commandName in listOf(
                ActionsBundle.message("action.GotoDeclaration.text"),
                ActionsBundle.message("action.GotoDeclarationOnly.text"),
                ActionsBundle.message("action.GotoTypeDeclaration.text"),
            )
        ) {
            event.project?.let { project ->
                FileEditorManagerEx.getInstanceEx(project).currentFile?.let { virtualFile ->
                    FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(virtualFile)?.let { fileEditor ->
                        (fileEditor as? TextEditor)?.editor?.let { editor ->
                            (editor as? EditorEx)?.let { ex ->
                                val offset = ex.caretModel.primaryCaret.offset
                                val placeInfo = PlaceInfoWrapper(
                                    PlaceInfo(
                                        virtualFile,
                                        fileEditor.getState(FileEditorStateLevel.UNDO),
                                        editorTypeId ?: TextEditorProvider.getInstance().editorTypeId,
                                        null,
                                        ex.document.createRangeMarker(offset, offset)
                                    )
                                )
                                val lastPlaceInfo = places.peek()
                                if (lastPlaceInfo == null || lastPlaceInfo != placeInfo) {
                                    places.push(placeInfo)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
