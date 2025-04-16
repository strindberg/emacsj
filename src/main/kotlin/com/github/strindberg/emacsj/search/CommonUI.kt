package com.github.strindberg.emacsj.search

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import kotlin.concurrent.thread
import com.intellij.codeInsight.hint.HintUtil
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

internal class CommonUI(
    val editor: Editor,
    private var writeable: Boolean,
    cancelCallback: () -> Unit,
    keyEventHandler: (KeyEvent) -> Unit = { },
) {

    private val standardFont =
        UIUtil.getLabelFont().deriveFont((editor as? EditorEx)?.colorsScheme?.editorFontSize2D?.times(1.1f) ?: UIUtil.getLabelFont().size2D)

    private val panel: UIPanel = UIPanel(this, editor, standardFont)

    private val titleLabel = newLabel(false)

    private val textLabel = newLabel(true)

    private val spaceLabel1 = newLabel(false).apply { text = " " }

    private val spaceLabel2 = newLabel(false).apply { text = "  " }

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
        spaceLabel1.background = HintUtil.getInformationColor()
        titleLabel.background = HintUtil.getInformationColor()
        textField.background = HintUtil.getInformationColor()
        spaceLabel2.background = HintUtil.getInformationColor()
        countLabel.background = HintUtil.getInformationColor()

        panel.add(spaceLabel1, GridBagConstraints().apply { gridx = 0 })
        panel.add(titleLabel, GridBagConstraints().apply { gridx = 1 })

        if (writeable) {
            panel.add(
                textField,
                GridBagConstraints().apply {
                    gridx = 2
                    weightx = 1.0
                    fill = GridBagConstraints.HORIZONTAL
                }
            )
        } else {
            setReadonlyComponents()
        }

        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, if (writeable) textField else null)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(writeable)
            .setCancelCallback {
                cancelCallback()
                true
            }
            .setKeyEventHandler { event ->
                keyEventHandler(event)
                false
            }
            .createPopup()
    }

    internal fun flashLax(lax: Boolean) {
        if (lax) {
            countLabel.text = "[match spaces loosely]"
        } else {
            countLabel.text = "[match spaces literally]"
        }
        thread(start = true) {
            Thread.sleep(1500)
            countLabel.text = ""
        }
    }

    internal fun selectText() {
        textField.selectAll()
    }

    internal fun show() {
        popup.show(popupPoint())
    }

    internal fun cancelUI() {
        popup.cancel()
        panel.cancel()
    }

    internal fun popupPoint(): RelativePoint =
        SwingUtilities.getAncestorOfClass(JScrollPane::class.java, editor.contentComponent).let { scroll ->
            RelativePoint(scroll, Point(0, scroll.y + scroll.height - panel.preferredSize.height))
        }

    internal fun setPopupBounds(rectangle: Rectangle) {
        popup.setBounds(rectangle)
    }

    internal fun makeReadonly(text: String) {
        panel.remove(textField)
        setReadonlyComponents()

        textLabel.text = text
        writeable = false

        titleLabel.requestFocus()
    }

    private fun displayText(text: String): String = text.replace("\n", "\\n")

    private fun scrubText(text: String): String = text.replace("\\n", "\n")

    private fun newLabel(bold: Boolean): JLabel =
        JLabel("").apply {
            font = if (bold) standardFont.deriveFont(Font.BOLD) else standardFont
            background = HintUtil.getInformationColor()
            foreground = JBColor.foreground()
            isOpaque = true
        }

    private fun setReadonlyComponents() {
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
    }
}

private class UIPanel(private val commonUI: CommonUI, private val editor: Editor, private val baseFont: Font) : JPanel(GridBagLayout()) {

    private val resizeListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            commonUI.setPopupBounds(getNewBounds())
        }
    }

    private val moveListener = object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent?) {
            commonUI.setPopupBounds(getNewBounds())
        }
    }

    init {
        editor.component.addComponentListener(resizeListener)
        editor.component.topLevelAncestor?.addComponentListener(moveListener)
    }

    override fun getPreferredSize(): Dimension = Dimension(editor.component.width, (baseFont.size * 2.5).toInt())

    fun getNewBounds(): Rectangle = Rectangle(commonUI.popupPoint().screenPoint, preferredSize)

    fun cancel() {
        editor.component.removeComponentListener(resizeListener)
        editor.component.topLevelAncestor?.removeComponentListener(moveListener)
    }
}
