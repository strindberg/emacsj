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

    fun addAction(actionId: String)

    fun lastActionIds(): ActionIds

    fun lastActionId(): String?

    fun isLastStrictUniversal(): Boolean

    fun isLastUniversal(): Boolean

    fun setRepeating(repeating: Boolean)

    fun isRepeating(): Boolean

    /**
     * Whether an action is currently being performed. Editor actions raise a command as well, and this lets the
     * command listener leave those to the action listener instead of recording them twice.
     */
    fun setPerformingAction(performing: Boolean)

    fun isPerformingAction(): Boolean

    fun registerSingleAction(actionId: String)

    fun getSingleActions(): Set<String>
}
