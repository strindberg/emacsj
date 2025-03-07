package com.github.strindberg.emacsj.preferences

import javax.swing.JComponent
import javax.swing.JPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder

class EmacsJSettingsComponent {

    private val mainPanel: JPanel

    private val searchWhitespaceRegexp: JBTextField = JBTextField()

    init {
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Whitespace regexp:"), searchWhitespaceRegexp, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel = mainPanel

    fun getPreferredFocusedComponent(): JComponent = searchWhitespaceRegexp

    fun getSearchWhitespaceRegexp(): String = searchWhitespaceRegexp.getText()

    fun setSearchWhitespaceRegexp(newText: String) {
        searchWhitespaceRegexp.setText(newText)
    }
}
