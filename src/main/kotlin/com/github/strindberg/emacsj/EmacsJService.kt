package com.github.strindberg.emacsj

import com.intellij.openapi.application.ApplicationManager

interface EmacsJService {

    companion object {
        val instance
            get(): EmacsJService = ApplicationManager.getApplication().getService(EmacsJServiceImpl::class.java)
    }

    fun addCommand(commandName: String)

    fun lastCommandName(): String?

    fun lastCommandNames(): CommandNames

    fun isLastStrictUniversal(): Boolean

    fun isLastUniversal(): Boolean

    fun isLastNumericUniversal(): Boolean

    fun isPreviousUniversal(): Boolean
}
