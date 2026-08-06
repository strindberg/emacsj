package com.github.strindberg.emacsj

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.singleActions
import com.github.strindberg.emacsj.universal.universalActionIds
import com.intellij.openapi.components.Service

/**
 * Holds global action state. Most callers are on the EDT, but [com.github.strindberg.emacsj.ui.EmacsJActionsPromoter]
 * runs wherever the platform chooses to update actions, so the state here is made safe for any thread rather than
 * relying on an EDT-confinement invariant that nothing enforces.
 */
@Service
class EmacsJServiceImpl : EmacsJService {

    private val lastActionIds = AtomicReference(ActionIds(null, null))

    @Volatile
    private var lastArgument = 1

    @Volatile
    private var isRepeating = false

    @Volatile
    private var isPerformingAction = false

    private val registeredSingleActions = ConcurrentHashMap.newKeySet<String>().apply { addAll(singleActions) }

    override fun addAction(actionId: String) {
        lastActionIds.updateAndGet { ActionIds(last = actionId, previous = it.last) }
    }

    override fun registerUniversalArgument(lastArgument: Int) {
        this.lastArgument = lastArgument
    }

    override fun universalArgument() = if (isLastUniversal()) lastArgument else 1

    override fun universalArgumentRelaxed() =
        lastActionIds.get().let { ids ->
            if (ids.last in universalActionIds || ids.previous in universalActionIds) lastArgument else 1
        }

    override fun lastActionIds(): ActionIds = lastActionIds.get()

    override fun lastActionId() = lastActionIds.get().last

    override fun isLastStrictUniversal() = lastActionIds.get().last == ACTION_UNIVERSAL_ARGUMENT

    override fun isLastUniversal() = lastActionIds.get().last in universalActionIds

    override fun setRepeating(repeating: Boolean) {
        this.isRepeating = repeating
    }

    override fun isRepeating() = isRepeating

    override fun setPerformingAction(performing: Boolean) {
        this.isPerformingAction = performing
    }

    override fun isPerformingAction() = isPerformingAction

    override fun registerSingleAction(actionId: String) {
        registeredSingleActions.add(actionId)
    }

    override fun getSingleActions() = registeredSingleActions.toSet()
}

data class ActionIds(val last: String?, val previous: String?)
