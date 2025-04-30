package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.mark.LimitedStack
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.mark.PlaceInfo
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx

class EmacsJCommandListener : CommandListener {

    companion object {
        private val places = LimitedStack<PlaceInfo>()

        internal var lastCommandName: String? = null
            private set

        internal fun popPlace(): PlaceInfo? = places.pop()
    }

    override fun commandFinished(event: CommandEvent) {
        // Empty or "Undefined" commands are present when running tests
        if (event.commandName != "" && event.commandName != "Undefined") {
            lastCommandName = event.commandName
        }
    }

    override fun commandStarted(event: CommandEvent) {
        if (event.commandName in listOf(
                ActionsBundle.message("action.GotoDeclaration.text"),
                ActionsBundle.message("action.GotoDeclarationOnly.text"),
                ActionsBundle.message("action.GotoTypeDeclaration.text"),
            )
        ) {
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
}
