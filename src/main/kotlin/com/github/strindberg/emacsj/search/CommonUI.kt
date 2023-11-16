package com.github.strindberg.emacsj.search

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Point
import java.awt.event.KeyEvent
import java.lang.invoke.MethodHandles
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.VisibleForTesting

@Suppress("unused")
private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

internal class CommonUI(val editor: Editor, keyEventHandler: (KeyEvent) -> Boolean, cancelCallback: () -> Boolean, var writeable: Boolean) {

    private val standardFont = UIUtil.getLabelFont()

    private val panel: UIPanel = UIPanel(editor.component, standardFont)

    private val titleLabel = newLabel(false)

    private val textLabel = newLabel(true)

    private val spaceLabel = newLabel(false).apply { text = "  " }

    private val countLabel = newLabel(false)

    @VisibleForTesting
    internal val textField = object : LanguageTextField(PlainTextLanguage.INSTANCE, editor.project, "") {
        override fun createEditor(): EditorEx =
            super.createEditor().apply {
                SpellCheckingEditorCustomizationProvider.getInstance().getCustomization(false)?.customize(this)
            }
    }

    internal var popup: JBPopup

    internal var title: String
        get() = titleLabel.text
        set(newText) {
            titleLabel.text = newText
        }

    internal var text: String
        get() = scrubText(if (writeable) textField.text else textLabel.text)
        set(newText) {
            if (writeable) {
                textField.text = displayText(newText)
                textField.setCaretPosition(textField.text.length)
                textField.removeSelection()
            } else {
                textLabel.text = displayText(newText)
            }
        }

    private fun displayText(text: String): String = text.replace("\n", "\\n")

    private fun scrubText(text: String): String = text.replace("\\n", "\n")

    internal var count: Pair<Int, Int>? = null
        set(newCount) {
            field = newCount
            countLabel.text = newCount?.let { "(${newCount.first}/${newCount.second})" } ?: ""
        }

    internal var textColor: Color
        get() = if (writeable) textField.foreground else textLabel.foreground
        set(newColor) {
            if (writeable) textField.foreground = newColor else textLabel.foreground = newColor
        }

    init {
        panel.background = HintUtil.getInformationColor()
        titleLabel.background = HintUtil.getInformationColor()
        textField.background = HintUtil.getInformationColor()
        spaceLabel.background = HintUtil.getInformationColor()
        countLabel.background = HintUtil.getInformationColor()

        panel.add(titleLabel, GridBagConstraints().apply { gridx = 0 })

        if (writeable) {
            panel.add(
                textField,
                GridBagConstraints().apply {
                    gridx = 1
                    weightx = 1.0
                    fill = GridBagConstraints.HORIZONTAL
                }
            )
        } else {
            setReadonlyComponents()
        }

        popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, if (writeable) textField else null)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(writeable)
            .setCancelCallback(cancelCallback)
            .setKeyEventHandler(keyEventHandler)
            .createPopup()
    }

    private fun newLabel(bold: Boolean): JLabel =
        JLabel("").apply {
            font = if (bold) standardFont.deriveFont(Font.BOLD) else standardFont
            background = HintUtil.getInformationColor()
            foreground = JBColor.foreground()
            isOpaque = true
        }

    internal fun selectText() {
        textField.selectAll()
    }

    internal fun show() {
        val scroll = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, editor.contentComponent)
        val point = Point(0, scroll.y + scroll.height - panel.preferredSize.height)
        popup.show(RelativePoint(scroll, point))
    }

    internal fun makeReadonly(text: String) {
        panel.remove(textField)
        setReadonlyComponents()

        textLabel.text = text
        writeable = false

        titleLabel.requestFocus()
    }

    private fun setReadonlyComponents() {
        panel.add(textLabel, GridBagConstraints().apply { gridx = 1 })
        panel.add(spaceLabel, GridBagConstraints().apply { gridx = 2 })
        panel.add(
            countLabel,
            GridBagConstraints().apply {
                gridx = 3
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
            }
        )
    }
}

private class UIPanel(private val contentComponent: JComponent, val standardFont: Font) : JPanel(GridBagLayout()) {
    override fun getPreferredSize(): Dimension {
        return Dimension(contentComponent.width, (standardFont.size * 2.5).toInt())
    }
}
