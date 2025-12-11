package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.universal.universalCommandName
import com.github.strindberg.emacsj.universal.universalCommandNames

class EmacsJServiceImpl : EmacsJService {

    private var lastCommandNames: CommandNames = CommandNames(null, null)

    private var lastArgument = 1

    private var repeating = false

    override fun addCommand(commandName: String) {
        synchronized(this) {
            lastCommandNames = CommandNames(commandName, lastCommandNames.last)
        }
    }

    override fun registerUniversalArgument(lastArgument: Int) {
        this.lastArgument = lastArgument
    }

    override fun universalArgument(): Int = if (isLastUniversal()) lastArgument else 1

    override fun universalArgumentRelaxed(): Int =
        if (isLastUniversal() || lastCommandNames.previous in universalCommandNames) lastArgument else 1

    override fun lastCommandNames() = lastCommandNames

    override fun lastCommandName() = lastCommandNames.last

    override fun isLastStrictUniversal() = lastCommandNames.last == universalCommandName

    override fun isLastUniversal() = lastCommandNames.last in universalCommandNames

    override fun setRepeating(repeating: Boolean) {
        this.repeating = repeating
    }

    override fun isRepeating() = repeating
}

data class CommandNames(val last: String?, val previous: String?)
