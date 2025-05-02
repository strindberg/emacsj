package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.xref.XRefHandler
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener

class EmacsJCommandListener : CommandListener {

    companion object {
        internal var lastCommandName: String? = null
            private set
    }

    override fun commandFinished(event: CommandEvent) {
        // Empty or "Undefined" commands are present when running tests
        if (event.commandName.isNotBlank() && event.commandName != "Undefined") {
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
            XRefHandler.pushPlace(event)
        }
    }
}
