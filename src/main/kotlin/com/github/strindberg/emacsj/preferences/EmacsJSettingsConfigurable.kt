package com.github.strindberg.emacsj.preferences

import javax.swing.JComponent
import com.intellij.openapi.options.Configurable

class EmacsJSettingsConfigurable : Configurable {

    var component: EmacsJSettingsComponent = EmacsJSettingsComponent()

    override fun getDisplayName(): String = "EmacsJ"

    override fun createComponent(): JComponent {
        component = EmacsJSettingsComponent()
        return component.getPanel()
    }

    override fun getPreferredFocusedComponent(): JComponent = component.getPreferredFocusedComponent()

    override fun isModified(): Boolean =
        component.getSearchWhitespaceRegexp() != EmacsJSettings.getInstance().getState().searchWhitespaceRegexp

    override fun reset() {
        component.setSearchWhitespaceRegexp(EmacsJSettings.getInstance().getState().searchWhitespaceRegexp)
    }

    override fun apply() {
        EmacsJSettings.getInstance().getState().searchWhitespaceRegexp = component.getSearchWhitespaceRegexp()
    }
}
