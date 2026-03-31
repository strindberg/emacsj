package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.universal.singleActions
import com.github.strindberg.emacsj.universal.universalCommandName
import com.github.strindberg.emacsj.universal.universalCommandNames
import com.intellij.openapi.components.Service

@Service
class EmacsJServiceImpl : EmacsJService {

    private var lastCommandNames = CommandNames(null, null)

    private var lastArgument = 1

    private var isRepeating = false

    private val registeredSingleActions = singleActions.toMutableSet()

    override fun addCommand(commandName: String) {
        synchronized(this) {
            lastCommandNames = CommandNames(last = commandName, previous = lastCommandNames.last)
        }
    }

    override fun registerUniversalArgument(lastArgument: Int) {
        this.lastArgument = lastArgument
    }

    override fun universalArgument() = if (isLastUniversal()) lastArgument else 1

    override fun universalArgumentRelaxed() =
        if (isLastUniversal() || lastCommandNames.previous in universalCommandNames) lastArgument else 1

    override fun lastCommandNames() = lastCommandNames

    override fun lastCommandName() = lastCommandNames.last

    override fun isLastStrictUniversal() = lastCommandNames.last == universalCommandName

    override fun isLastUniversal() = lastCommandNames.last in universalCommandNames

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
