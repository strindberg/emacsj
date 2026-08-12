package com.github.strindberg.emacsj

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

/** The plugin's coroutine scope, for work that outlives a keystroke but has no service of its own to belong to. */
@Service
internal class EmacsJScope(val scope: CoroutineScope) {

    companion object {
        internal val instance: EmacsJScope
            get() = ApplicationManager.getApplication().getService(EmacsJScope::class.java)
    }
}
