package com.github.strindberg.emacsj

import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener

/**
 * Records everything that reaches the editor without being an action -- typing, mouse edits, commands started in
 * code. Actions are recorded by [EmacsJActionListener] instead, by id; editor
 * actions raise a command too, so those are skipped here to avoid recording them twice.
 */
internal class EmacsJCommandListener : CommandListener {

    override fun commandFinished(event: CommandEvent) {
        if (EmacsJService.instance.isPerformingAction()) return

        // Empty, "Undefined" or "Dummy" commands are present when running tests
        if (!event.commandName.isNullOrBlank() && event.commandName !in listOf("Undefined", "Dummy")) {
            EmacsJService.instance.addAction(event.commandName)
        }
    }
}
