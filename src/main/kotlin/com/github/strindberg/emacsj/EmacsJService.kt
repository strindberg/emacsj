package com.github.strindberg.emacsj

import com.intellij.openapi.application.ApplicationManager

interface EmacsJService {

    companion object {
        val instance
            get(): EmacsJService = ApplicationManager.getApplication().getService(EmacsJServiceImpl::class.java)
    }

    fun registerUniversalArgument(lastArgument: Int)

    fun universalArgument(): Int

    fun universalArgumentRelaxed(): Int

    fun addCommand(commandName: String)

    fun lastCommandNames(): CommandNames

    fun lastCommandName(): String?

    fun isLastStrictUniversal(): Boolean

    fun isLastUniversal(): Boolean

    fun setRepeating(repeating: Boolean)

    fun isRepeating(): Boolean
}
