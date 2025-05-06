package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.xref.XRefHandler
import com.intellij.idea.ActionsBundle
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener

class EmacsJCommandListener : CommandListener {

    companion object {
        internal var lastCommandNames: Pair<String?, String?> = Pair(null, null)
            private set

        internal var lastCommandName: String? = null
            get() = lastCommandNames.first
            private set
    }

    override fun commandFinished(event: CommandEvent) {
        // Empty or "Undefined" commands are present when running tests
        if (!event.commandName.isNullOrBlank() && event.commandName != "Undefined") {
            lastCommandNames = Pair(event.commandName, lastCommandNames.first)
        }
    }

    override fun commandStarted(event: CommandEvent) {
        if (event.commandName in listOf(
                ActionsBundle.actionText("GotoDeclaration"),
                ActionsBundle.actionText("GotoDeclarationOnly"),
                ActionsBundle.actionText("GotoTypeDeclaration"),
                ActionsBundle.actionText("GotoSuperMethod"),
            )
        ) {
            XRefHandler.pushPlace(event)
        }
    }
}
