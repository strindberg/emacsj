package com.github.strindberg.emacsj.preferences

import java.awt.Font
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder

class EmacsJSettingsComponent {

    private val mainPanel: JPanel

    private val iSearchLabel = JBLabel("Isearch")

    private val searchWhitespaceRegexp: JBTextField = JBTextField()

    private val useLaxISearch: JCheckBox = JCheckBox()

    private val description =
        JBLabel("If enabled, space is replaced with given regexp in Isearch. Default is '.*?'. Emacs default is '[ 	]+'.")

    init {
        description.font = description.font.deriveFont(Font.ITALIC)
        iSearchLabel.font = iSearchLabel.font.deriveFont(Font.BOLD)
        mainPanel = FormBuilder.createFormBuilder()
            .addComponent(iSearchLabel)
            .addLabeledComponent(JBLabel("Isearch whitespace regexp:"), searchWhitespaceRegexp, 1, false)
            .addLabeledComponent(JBLabel("Use lax Isearch:"), useLaxISearch, 1, false)
            .addComponent(description)
            .addSeparator()
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel = mainPanel

    fun getPreferredFocusedComponent(): JComponent = searchWhitespaceRegexp

    fun getSearchWhitespaceRegexp(): String = searchWhitespaceRegexp.getText()

    fun setSearchWhitespaceRegexp(newText: String) {
        searchWhitespaceRegexp.setText(newText)
    }

    fun getUseLaxISearch() = useLaxISearch.isSelected

    fun setUseLaxISearch(newValue: Boolean) {
        useLaxISearch.isSelected = newValue
    }
}
