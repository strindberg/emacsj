package com.github.strindberg.emacsj

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.singleActions
import com.github.strindberg.emacsj.universal.universalActionIds
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service

/**
 * Holds global action state. Most callers are on the EDT, but [com.github.strindberg.emacsj.ui.EmacsJActionsPromoter]
 * runs wherever the platform chooses to update actions, so the state here is made safe for any thread rather than
 * relying on an EDT-confinement invariant that nothing enforces.
 */
@Service
internal class EmacsJService {

    private val lastActionIds = AtomicReference(ActionIds(null, null))

    @Volatile
    private var lastArgument = 1

    @Volatile
    private var isRepeating = false

    @Volatile
    private var isPerformingAction = false

    private val registeredSingleActions = ConcurrentHashMap.newKeySet<String>().apply { addAll(singleActions) }

    fun addAction(actionId: String) {
        lastActionIds.updateAndGet { ActionIds(last = actionId, previous = it.last) }
    }

    fun registerUniversalArgument(lastArgument: Int) {
        this.lastArgument = lastArgument
    }

    fun universalArgument() = if (isLastUniversal()) lastArgument else 1

    fun universalArgumentRelaxed() =
        lastActionIds.get().let { ids ->
            if (ids.last in universalActionIds || ids.previous in universalActionIds) lastArgument else 1
        }

    fun lastActionIds(): ActionIds = lastActionIds.get()

    fun lastActionId() = lastActionIds.get().last

    fun isLastStrictUniversal() = lastActionIds.get().last == ACTION_UNIVERSAL_ARGUMENT

    fun isLastUniversal() = lastActionIds.get().last in universalActionIds

    fun setRepeating(repeating: Boolean) {
        this.isRepeating = repeating
    }

    fun isRepeating() = isRepeating

    /**
     * Whether an action is currently being performed. Editor actions raise a command as well, and this lets the
     * command listener leave those to the action listener instead of recording them twice.
     */
    fun setPerformingAction(performing: Boolean) {
        this.isPerformingAction = performing
    }

    fun isPerformingAction() = isPerformingAction

    fun registerSingleAction(actionId: String) {
        registeredSingleActions.add(actionId)
    }

    fun getSingleActions() = registeredSingleActions.toSet()

    companion object {
        internal val instance: EmacsJService
            get() = ApplicationManager.getApplication().getService(EmacsJService::class.java)
    }
}

internal data class ActionIds(val last: String?, val previous: String?)
