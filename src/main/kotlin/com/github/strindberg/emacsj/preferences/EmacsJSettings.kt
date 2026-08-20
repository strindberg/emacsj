package com.github.strindberg.emacsj.preferences

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "com.github.strindberg.emacsj.EmacsJSettings",
    storages = [Storage("EmacsJSettings.xml")],
)
@Service
internal class EmacsJSettings : PersistentStateComponent<EmacsJState> {

    private var state = EmacsJState()

    override fun getState(): EmacsJState = state

    override fun loadState(state: EmacsJState) {
        this.state = state
    }

    companion object {
        internal val instance: EmacsJSettings
            get() = ApplicationManager.getApplication().getService(EmacsJSettings::class.java)
    }
}

class EmacsJState(var searchWhitespaceRegexp: String = ".*?", var useLaxISearch: Boolean = false, var useSelectionISearch: Boolean = false)
