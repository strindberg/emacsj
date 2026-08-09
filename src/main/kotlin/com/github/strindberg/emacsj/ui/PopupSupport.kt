package com.github.strindberg.emacsj.ui

import java.awt.Container
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import com.intellij.openapi.editor.Editor
import com.intellij.ui.awt.RelativePoint

internal interface PopupUI {

    fun cancelUI()
}

/** The scroll pane every EmacsJ popup is positioned against: the one holding the editor's content. */
internal val Editor.popupAnchor: JScrollPane
    get() = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, contentComponent) as JScrollPane

/** Bottom left of the editor's visible area, where EmacsJ popups sit. */
internal fun popupPointIn(anchor: JScrollPane, popupHeight: Int): RelativePoint =
    RelativePoint(anchor, Point(0, anchor.height - popupHeight))

/** Keeps a popup pinned to the bottom of the editor while the window is resized or moved. */
internal class PopupBoundsListener(editor: Editor, onGeometryChanged: () -> Unit) {

    private val resizeListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            onGeometryChanged()
        }
    }

    private val moveListener = object : ComponentAdapter() {
        override fun componentMoved(e: ComponentEvent?) {
            onGeometryChanged()
        }
    }

    private val ancestor: Container = editor.component

    init {
        ancestor.addComponentListener(resizeListener)
        ancestor.addComponentListener(moveListener)
    }

    fun detach() {
        ancestor.removeComponentListener(resizeListener)
        ancestor.removeComponentListener(moveListener)
    }
}
