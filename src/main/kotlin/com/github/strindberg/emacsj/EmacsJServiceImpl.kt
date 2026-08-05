package com.github.strindberg.emacsj

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import com.github.strindberg.emacsj.universal.singleActions
import com.github.strindberg.emacsj.universal.universalCommandName
import com.github.strindberg.emacsj.universal.universalCommandNames
import com.intellij.openapi.components.Service

/**
 * Holds global command state. Most callers are on the EDT, but [com.github.strindberg.emacsj.ui.EmacsJActionsPromoter]
 * runs wherever the platform chooses to update actions, so the state here is made safe for any thread rather than
 * relying on an EDT-confinement invariant that nothing enforces.
 */
@Service
class EmacsJServiceImpl : EmacsJService {

    private val lastCommandNames = AtomicReference(CommandNames(null, null))

    @Volatile
    private var lastArgument = 1

    @Volatile
    private var isRepeating = false

    private val registeredSingleActions = ConcurrentHashMap.newKeySet<String>().apply { addAll(singleActions) }

    override fun addCommand(commandName: String) {
        lastCommandNames.updateAndGet { CommandNames(last = commandName, previous = it.last) }
    }

    override fun registerUniversalArgument(lastArgument: Int) {
        this.lastArgument = lastArgument
    }

    override fun universalArgument() = if (isLastUniversal()) lastArgument else 1

    override fun universalArgumentRelaxed() =
        lastCommandNames.get().let { names ->
            if (names.last in universalCommandNames || names.previous in universalCommandNames) lastArgument else 1
        }

    override fun lastCommandNames(): CommandNames = lastCommandNames.get()

    override fun lastCommandName() = lastCommandNames.get().last

    override fun isLastStrictUniversal() = lastCommandNames.get().last == universalCommandName

    override fun isLastUniversal() = lastCommandNames.get().last in universalCommandNames

    override fun setRepeating(repeating: Boolean) {
        this.isRepeating = repeating
    }

    override fun isRepeating() = isRepeating

    override fun registerSingleAction(actionId: String) {
        registeredSingleActions.add(actionId)
    }

    override fun getSingleActions() = registeredSingleActions.toSet()
}

data class CommandNames(val last: String?, val previous: String?)
