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

    private val repeatedMacroLabel = JBLabel("Repeated macros")

    private val repeatedMacroTimeout: JBTextField = JBTextField()

    private val description1 =
        JBLabel("If enabled, whitespace is replaced with this regexp in Isearch. Default is '.*?'. Emacs default is '[ 	]+'.")

    private val description2 =
        JBLabel("Lax Isearch can be toggled with key binding 'Toggle Lax-Whitespace Searching'.")

    private val description3 =
        JBLabel("The number of seconds a macro is repeated before it is aborted. Default is 180.")

    private val description4 =
        JBLabel("Consider setting the IDE's macro typing speed to a lower number than the default 200 in the Registry.")

    init {
        iSearchLabel.font = iSearchLabel.font.deriveFont(Font.BOLD)
        repeatedMacroLabel.font = iSearchLabel.font.deriveFont(Font.BOLD)

        description1.font = description1.font.deriveFont(Font.ITALIC)
        description2.font = description2.font.deriveFont(Font.ITALIC)
        description3.font = description3.font.deriveFont(Font.ITALIC)
        description4.font = description4.font.deriveFont(Font.ITALIC)

        mainPanel = FormBuilder.createFormBuilder()
            .addSeparator()
            .addComponent(iSearchLabel)
            .addLabeledComponent(JBLabel("Isearch whitespace regexp:"), searchWhitespaceRegexp, 1, false)
            .addLabeledComponent(JBLabel("Use lax Isearch:"), useLaxISearch, 1, false)
            .addComponent(description1)
            .addComponent(description2)
            .addVerticalGap(description1.font.size)
            .addSeparator()
            .addComponent(repeatedMacroLabel)
            .addLabeledComponent(JBLabel("Repeated macro timeout:"), repeatedMacroTimeout, 1, false)
            .addComponent(description3)
            .addComponent(description4)
            .addVerticalGap(description1.font.size)
            .addSeparator()
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    fun getPanel(): JPanel = mainPanel

    fun getPreferredFocusedComponent(): JComponent = searchWhitespaceRegexp

    fun getSearchWhitespaceRegexp(): String = searchWhitespaceRegexp.getText()

    fun setSearchWhitespaceRegexp(newText: String) {
        searchWhitespaceRegexp.text = newText
    }

    fun getUseLaxISearch() = useLaxISearch.isSelected

    fun setUseLaxISearch(newValue: Boolean) {
        useLaxISearch.isSelected = newValue
    }

    fun getRepeatedMacroTimeout() = repeatedMacroTimeout.getText().toLong()

    fun setRepeatedMacroTimeout(newValue: Long) {
        repeatedMacroTimeout.text = newValue.toString()
    }
}
