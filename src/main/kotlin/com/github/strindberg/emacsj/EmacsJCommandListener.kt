package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.mark.LimitedStack
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider

class EmacsJCommandListener : CommandListener {

    companion object {
        private val places = LimitedStack<PlaceInfo>()

        private var lastCommandName: String? = null

        // Used for testing
        internal var editorTypeId: String? = null

        fun popPlace(): PlaceInfo? = places.pop()

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
                val fileEditorManager = FileEditorManagerEx.getInstanceEx(project)
                fileEditorManager.currentFile?.let { virtualFile ->
                    fileEditorManager.getSelectedEditor(virtualFile)?.let { fileEditor ->
                        (fileEditor as? TextEditor)?.editor?.let { editor ->
                            places.push(
                                PlaceInfo(
                                    virtualFile,
                                    fileEditor.getState(FileEditorStateLevel.UNDO),
                                    editorTypeId ?: TextEditorProvider.getInstance().editorTypeId,
                                    editor.caretModel.primaryCaret.offset,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
