package com.github.strindberg.emacsj.ui

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Rectangle
import java.awt.event.KeyEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import com.github.strindberg.emacsj.EmacsJScope
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SpellCheckingEditorCustomizationProvider
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.text.StringUtil.escapeXmlEntities
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.LanguageTextField
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.UIUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.VisibleForTesting

private const val FLASH_MILLIS = 1500L

internal class CommonUI(
    private val editor: Editor,
    private var isWriteable: Boolean,
    private val cancelCallback: () -> Unit,
    private val keyEventHandler: (KeyEvent) -> Unit = { },
) : PopupUI {

    private val standardFont =
        UIUtil.getLabelFont().deriveFont(
            (editor as? EditorEx)?.run {
                colorsScheme.editorFontSize2D.times(1.1f)
            } ?: UIUtil.getLabelFont().size2D
        )

    private val panel: UIPanel = UIPanel(this, editor, standardFont)

    private val titleLabel = newLabel(false)

    private val textLabel = newLabel(true)

    private val spaceLabel1 = newLabel(false).apply { text = " " }

    private val spaceLabel2 = newLabel(false).apply { text = "  " }

    private val countLabel = newLabel(false)

    private var flash: Job? = null

    // What the read-only label stands for. The label itself may hold markup, so it cannot be read back as the value.
    private var readonlyText: String = ""

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
        get() = if (isWriteable) scrubText(textField.text) else readonlyText
        set(newText) {
            if (isWriteable) {
                textField.text = displayText(newText)
                textField.setCaretPosition(textField.text.length)
                textField.removeSelection()
            } else {
                readonlyText = newText
                textLabel.text = displayText(newText)
            }
        }

    internal fun showText(found: String, notFound: String = "") {
        readonlyText = found + notFound
        textLabel.text = if (notFound.isEmpty()) {
            displayText(readonlyText)
        } else {
            buildString {
                append("<html>")
                append(escapeXmlEntities(displayText(found)))
                append("""<font color="${ColorUtil.toHtmlColor(JBColor.RED)}">""")
                append(escapeXmlEntities(displayText(notFound)))
                append("</font></html>")
            }
        }
    }

    internal val markup: String
        @VisibleForTesting get() = textLabel.text

    internal var count: Pair<Int, Int>? = null
        set(newCount) {
            field = newCount
            countLabel.text = newCount?.let { "(${newCount.first}/${newCount.second})" }.orEmpty()
        }

    internal var textColor: Color
        get() = if (isWriteable) textField.foreground else textLabel.foreground
        set(newColor) {
            if (isWriteable) textField.foreground = newColor else textLabel.foreground = newColor
        }

    init {
        panel.background = HintUtil.getInformationColor()
        spaceLabel1.background = HintUtil.getInformationColor()
        titleLabel.background = HintUtil.getInformationColor()
        textField.background = HintUtil.getInformationColor()
        spaceLabel2.background = HintUtil.getInformationColor()
        countLabel.background = HintUtil.getInformationColor()

        panel.add(spaceLabel1, GridBagConstraints().apply { gridx = 0 })
        panel.add(titleLabel, GridBagConstraints().apply { gridx = 1 })

        if (isWriteable) {
            setWriteableComponents(text)
        } else {
            setReadonlyComponents(text)
        }

        popup = initPopup()
    }

    internal fun flashText(message: String, finalText: String = "") {
        countLabel.text = message
        // Only the newest flash may clear the label, which cancelling the previous one is enough to arrange.
        flash?.cancel()
        flash = EmacsJScope.instance.scope.launch {
            delay(FLASH_MILLIS)
            withContext(Dispatchers.EDT) { countLabel.text = finalText }
        }
    }

    internal fun selectText() {
        textField.selectAll()
    }

    internal fun show() {
        popup.show(popupPoint())
    }

    override fun cancelUI() {
        flash?.cancel() // Drop any pending flash so it cannot write to a closed popup.
        popup.cancel()
        panel.cancel()
    }

    internal val anchor: JScrollPane
        get() = editor.popupAnchor

    internal fun popupPoint(): RelativePoint = popupPointIn(anchor, panel.preferredSize.height)

    internal fun setPopupBounds(rectangle: Rectangle) {
        popup.setBounds(rectangle)
    }

    internal fun makeReadonly(newText: String, requestFocus: Boolean) {
        isWriteable = false

        setReadonlyComponents(newText)

        if (requestFocus) {
            titleLabel.requestFocus()
        }
    }

    internal fun makeWriteable(text: String) {
        isWriteable = true

        setWriteableComponents(text)

        recreatePopup()
    }

    private fun recreatePopup() {
        popup.cancel()
        popup = initPopup()
        show()
    }

    private fun initPopup(): JBPopup =
        JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, if (isWriteable) textField else null)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(isWriteable)
            .setCancelCallback {
                cancelCallback()
                true
            }
            .setKeyEventHandler { event ->
                keyEventHandler(event)
                false
            }
            .createPopup()

    private fun displayText(text: String): String = text.replace("\n", "\\n")

    private fun scrubText(text: String): String = text.replace("\\n", "\n")

    private fun newLabel(bold: Boolean): JLabel =
        JLabel("").apply {
            font = if (bold) standardFont.deriveFont(Font.BOLD) else standardFont
            background = HintUtil.getInformationColor()
            foreground = JBColor.foreground()
            isOpaque = true
        }

    private fun setReadonlyComponents(newText: String) {
        readonlyText = newText

        panel.remove(textField)

        panel.add(textLabel, GridBagConstraints().apply { gridx = 2 })
        panel.add(spaceLabel2, GridBagConstraints().apply { gridx = 3 })
        panel.add(
            countLabel,
            GridBagConstraints().apply {
                gridx = 4
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
            }
        )

        textLabel.text = newText

        panel.repaint()
    }

    private fun setWriteableComponents(newText: String) {
        panel.remove(textLabel)
        panel.remove(spaceLabel2)
        panel.remove(countLabel)

        textField.text = newText

        panel.add(
            textField,
            GridBagConstraints().apply {
                gridx = 2
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
            }
        )
    }
}

private class UIPanel(private val commonUI: CommonUI, editor: Editor, private val baseFont: Font) : JPanel(GridBagLayout()) {

    private val boundsListener = PopupBoundsListener(editor) { commonUI.setPopupBounds(getNewBounds()) }

    override fun getPreferredSize(): Dimension = Dimension(commonUI.anchor.width, (baseFont.size * 2.5).toInt())

    fun getNewBounds(): Rectangle = Rectangle(commonUI.popupPoint().screenPoint, preferredSize)

    fun cancel() {
        boundsListener.detach()
    }
}
