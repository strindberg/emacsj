package com.github.strindberg.emacsj.preferences

import javax.swing.JComponent
import com.github.strindberg.emacsj.search.ISearchHandler
import com.intellij.openapi.options.Configurable

class EmacsJSettingsConfigurable : Configurable {

    private var component = EmacsJSettingsComponent()

    override fun getDisplayName(): String = "EmacsJ"

    override fun createComponent(): JComponent {
        component = EmacsJSettingsComponent()
        return component.getPanel()
    }

    override fun getPreferredFocusedComponent(): JComponent = component.getPreferredFocusedComponent()

    override fun isModified(): Boolean =
        component.getSearchWhitespaceRegexp() != EmacsJSettings.getInstance().getState().searchWhitespaceRegexp ||
            component.getUseLaxISearch() != EmacsJSettings.getInstance().getState().useLaxISearch

    override fun reset() {
        component.setSearchWhitespaceRegexp(EmacsJSettings.getInstance().getState().searchWhitespaceRegexp)
        component.setUseLaxISearch(EmacsJSettings.getInstance().getState().useLaxISearch)
    }

    override fun apply() {
        EmacsJSettings.getInstance().getState().searchWhitespaceRegexp = component.getSearchWhitespaceRegexp()
        EmacsJSettings.getInstance().getState().useLaxISearch = component.getUseLaxISearch()
        ISearchHandler.lax = component.getUseLaxISearch()
    }
}
