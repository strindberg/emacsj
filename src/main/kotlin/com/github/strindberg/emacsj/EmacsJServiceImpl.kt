package com.github.strindberg.emacsj

import com.github.strindberg.emacsj.universal.numericUniversalCommandNames
import com.github.strindberg.emacsj.universal.universalCommandName
import com.github.strindberg.emacsj.universal.universalCommandNames

class EmacsJServiceImpl : EmacsJService {

    private var lastCommandNames: CommandNames = CommandNames(null, null)

    override fun addCommand(commandName: String) {
        synchronized(this) {
            lastCommandNames = CommandNames(commandName, lastCommandNames.last)
        }
    }

    override fun lastCommandName() = lastCommandNames.last

    override fun lastCommandNames() = lastCommandNames

    override fun isLastStrictUniversal() = lastCommandNames.last == universalCommandName

    override fun isLastUniversal() = lastCommandNames.last in universalCommandNames

    override fun isLastNumericUniversal() = lastCommandNames.last in numericUniversalCommandNames

    override fun isPreviousUniversal() = lastCommandNames.previous in universalCommandNames
}

data class CommandNames(val last: String?, val previous: String?)
