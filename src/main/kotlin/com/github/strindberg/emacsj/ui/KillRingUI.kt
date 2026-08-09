package com.github.strindberg.emacsj.ui

import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Rectangle
import java.awt.event.KeyEvent
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.ScrollPaneConstants
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.VisibleForTesting

private const val VISIBLE_ROWS = 10

private const val ELLIPSIS = "\u2026"

/** [text] cut down to fit [maxWidth], ending in an ellipsis when anything had to go. */
internal fun ellipsize(text: String, maxWidth: Int, metrics: FontMetrics): String =
    if (maxWidth <= 0 || metrics.stringWidth(text) <= maxWidth) {
        text
    } else {
        var kept = text.length
        while (kept > 0 && metrics.stringWidth(text.take(kept) + ELLIPSIS) > maxWidth) {
            kept--
        }
        text.take(kept) + ELLIPSIS
    }

/** Stands in for a line break, so that a multi-line entry still occupies exactly one row. */
private const val RETURN_SYMBOL = "↵"

/**
 * The kill ring shown as a list, one entry per line, at the bottom of the editor.
 *
 * Entries are rendered on a single line and clipped to the width of the editor; the untruncated text stays with the
 * delegate, so what is pasted is never what happens to fit on screen.
 */
internal class KillRingUI(
    private val editor: Editor,
    entries: List<String>,
    private val cancelCallback: () -> Unit,
    private val keyEventHandler: (KeyEvent) -> Boolean,
) : PopupUI {

    private val standardFont =
        UIUtil.getLabelFont().deriveFont(
            (editor as? EditorEx)?.run {
                colorsScheme.editorFontSize2D.times(1.1f)
            } ?: UIUtil.getLabelFont().size2D
        )

    private val titleLabel = JLabel("Yank from kill-ring: ").apply {
        font = standardFont.deriveFont(Font.BOLD)
        background = HintUtil.getInformationColor()
        foreground = JBColor.foreground()
        isOpaque = true
    }

    @VisibleForTesting
    internal val list = object : JBList<String>(entries.map { singleLine(it) }) {
        // Without this the list keeps its own, wider preferred width and the viewport simply cuts entries off at
        // the edge. Tracking the viewport makes every cell exactly as wide as the popup, which is what lets the
        // renderer's label end a long entry with an ellipsis instead.
        override fun getScrollableTracksViewportWidth(): Boolean = true
    }.apply {
        font = standardFont
        background = HintUtil.getInformationColor()
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = entries.size.coerceIn(1, VISIBLE_ROWS)
        if (entries.isNotEmpty()) {
            selectedIndex = 0
        }
    }

    private val cellRenderer = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component =
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).also { component ->
                val label = component as JLabel
                val available = list.width - label.insets.left - label.insets.right
                label.text = ellipsize((value as? String).orEmpty(), available, label.getFontMetrics(label.font))
            }
    }

    private val scrollPane = JBScrollPane(list).apply {
        // Entries are ellipsized rather than scrolled sideways; only the ring itself scrolls, and only vertically.
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        border = null
    }

    private val panel = KillRingPanel(this, editor)

    internal var popup: JBPopup

    internal var selectedIndex: Int
        get() = list.selectedIndex
        set(index) {
            list.selectedIndex = index
            list.ensureIndexIsVisible(index)
        }

    init {
        list.cellRenderer = cellRenderer

        panel.background = HintUtil.getInformationColor()
        panel.add(titleLabel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, list)
            .setCancelOnClickOutside(true)
            .setCancelOnOtherWindowOpen(true)
            .setMovable(false)
            .setResizable(false)
            .setRequestFocus(true)
            .setCancelCallback {
                cancelCallback()
                true
            }
            .setKeyEventHandler { event -> keyEventHandler(event) }
            .createPopup()
    }

    internal fun show() {
        popup.show(popupPointIn(editor.popupAnchor, popup.content.preferredSize.height))
    }

    override fun cancelUI() {
        popup.cancel()
        panel.cancel()
    }

    private fun singleLine(entry: String): String = entry.replace("\n", RETURN_SYMBOL)
}

private class KillRingPanel(killRingUI: KillRingUI, editor: Editor) : JPanel(BorderLayout()) {

    private val boundsListener = PopupBoundsListener(editor) { killRingUI.popup.setBounds(newBounds()) }

    private val anchor = editor.popupAnchor

    // Height is whatever the title and the list actually need -- computed from real cell heights.
    override fun getPreferredSize(): Dimension = Dimension(anchor.width, super.getPreferredSize().height)

    fun newBounds(): Rectangle = Rectangle(popupPointIn(anchor, preferredSize.height).screenPoint, preferredSize)

    fun cancel() {
        boundsListener.detach()
    }
}
