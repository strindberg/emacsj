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
        component.getSearchWhitespaceRegexp() != EmacsJSettings.instance.getState().searchWhitespaceRegexp ||
            component.getUseLaxISearch() != EmacsJSettings.instance.getState().useLaxISearch ||
            component.getUseSelectionISearch() != EmacsJSettings.instance.getState().useSelectionISearch

    override fun reset() {
        component.setSearchWhitespaceRegexp(EmacsJSettings.instance.getState().searchWhitespaceRegexp)
        component.setUseLaxISearch(EmacsJSettings.instance.getState().useLaxISearch)
        component.setUseSelectionISearch(EmacsJSettings.instance.getState().useSelectionISearch)
    }

    override fun apply() {
        EmacsJSettings.instance.getState().searchWhitespaceRegexp = component.getSearchWhitespaceRegexp()
        EmacsJSettings.instance.getState().useLaxISearch = component.getUseLaxISearch()
        EmacsJSettings.instance.getState().useSelectionISearch = component.getUseSelectionISearch()

        ISearchHandler.isLax = component.getUseLaxISearch()
        ISearchHandler.isSelectionISearch = component.getUseSelectionISearch()
    }
}
