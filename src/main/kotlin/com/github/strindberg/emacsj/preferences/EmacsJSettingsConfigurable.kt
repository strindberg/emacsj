package com.github.strindberg.emacsj.preferences

import javax.swing.JComponent
import com.github.strindberg.emacsj.search.ISearchHandler
import com.intellij.openapi.options.Configurable

internal class EmacsJSettingsConfigurable : Configurable {

    private var component = EmacsJSettingsComponent()

    override fun getDisplayName(): String = "EmacsJ"

    override fun createComponent(): JComponent {
        component = EmacsJSettingsComponent()
        return component.getPanel()
    }

    override fun getPreferredFocusedComponent(): JComponent = component.getPreferredFocusedComponent()

    override fun isModified(): Boolean =
        component.getSearchWhitespaceRegexp() != EmacsJSettings.getInstance().getState().searchWhitespaceRegexp ||
            component.getUseLaxISearch() != EmacsJSettings.getInstance().getState().useLaxISearch ||
            component.getUseSelectionISearch() != EmacsJSettings.getInstance().getState().useSelectionISearch

    override fun reset() {
        component.setSearchWhitespaceRegexp(EmacsJSettings.getInstance().getState().searchWhitespaceRegexp)
        component.setUseLaxISearch(EmacsJSettings.getInstance().getState().useLaxISearch)
        component.setUseSelectionISearch(EmacsJSettings.getInstance().getState().useSelectionISearch)
    }

    override fun apply() {
        EmacsJSettings.getInstance().getState().searchWhitespaceRegexp = component.getSearchWhitespaceRegexp()
        EmacsJSettings.getInstance().getState().useLaxISearch = component.getUseLaxISearch()
        EmacsJSettings.getInstance().getState().useSelectionISearch = component.getUseSelectionISearch()

        ISearchHandler.lax = component.getUseLaxISearch()
        ISearchHandler.selectionISearch = component.getUseSelectionISearch()
    }
}
