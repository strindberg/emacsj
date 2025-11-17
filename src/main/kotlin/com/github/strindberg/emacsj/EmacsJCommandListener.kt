package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.xref.XRefHandler
import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener

class EmacsJCommandListener : CommandListener {

    override fun commandFinished(event: CommandEvent) {
        // Empty, "Undefined" or "Dummy" commands are present when running tests
        if (!event.commandName.isNullOrBlank() && event.commandName != "Undefined" && event.commandName != "Dummy") {
            EmacsJService.instance.addCommand(event.commandName)
        }
    }

    override fun commandStarted(event: CommandEvent) {
        if (event.commandName in XRefHandler.xRefCommandNames) {
            XRefHandler.pushPlace(event)
        }
    }
}
