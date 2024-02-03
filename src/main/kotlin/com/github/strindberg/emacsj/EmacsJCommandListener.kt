package com.github.strindberg.emacsj

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

class EmacsJCommandListener : CommandListener {

    companion object {
        private val places = LimitedStack<PlaceInfoWrapper>()

        private var lastCommandName: String? = null

        // Used for testing
        internal var editorTypeId: String? = null

        fun popPlace(): PlaceInfoWrapper? = places.pop()

        fun lastCommandName() = lastCommandName
    }

    override fun commandFinished(event: CommandEvent) {
        lastCommandName = event.commandName
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
                                if (placeInfo != places.peek()) {
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
