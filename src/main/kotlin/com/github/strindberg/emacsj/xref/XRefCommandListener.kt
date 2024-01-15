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
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl
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
                FileEditorManagerEx.getInstanceEx(project).currentFile?.let { currentFile ->
                    FileEditorManagerEx.getInstanceEx(project).getSelectedEditor(currentFile)?.let { fileEditor ->
                        (fileEditor as? TextEditor)?.editor?.let { editor ->
                            (editor as? EditorEx)?.let { ex ->
                                val offset = ex.caretModel.primaryCaret.offset
                                places.push(
                                    PlaceInfoWrapper(
                                        IdeDocumentHistoryImpl.PlaceInfo(
                                            currentFile,
                                            fileEditor.getState(FileEditorStateLevel.UNDO),
                                            editorTypeId ?: TextEditorProvider.getInstance().editorTypeId,
                                            null,
                                            ex.document.createRangeMarker(offset, offset)
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
