package com.github.strindberg.emacsj.movement

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import com.github.strindberg.emacsj.mark.MarkHandler
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.ui.UIDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.Disposer
import org.jetbrains.annotations.VisibleForTesting

class GotoLineDelegate(override val editor: Editor) : UIDelegate {

    private var isDisposed = false

    @VisibleForTesting
    internal val ui = CommonUI(editor = editor, isWriteable = true, cancelCallback = ::hide, keyEventHandler = ::keyEventHandler).apply {
        title = "Go to line[:column] : "
    }

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            hide()
        }
    }

    init {
        EditorUtil.disposeWithEditor(editor, this)

        editor.caretModel.removeSecondaryCarets()
        editor.caretModel.addCaretListener(caretListener, this)

        ui.show()
    }

    private fun keyEventHandler(e: KeyEvent) {
        if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED && e.modifiersEx == 0) {
            val (line, column) = ui.text.split(":").let { parts ->
                try {
                    Pair(
                        parts.getOrNull(0)?.takeIf { it.isNotEmpty() }?.run { trim().toInt() },
                        parts.getOrNull(1)?.takeIf { it.isNotEmpty() }?.run { trim().toInt() }
                    )
                } catch (_: NumberFormatException) {
                    Pair(null, null)
                }
            }
            if (line != null) {
                val boundedLine = minOf(maxOf(0, line - 1), editor.document.lineCount - 1)
                val position =
                    if (column != null) {
                        minOf(
                            maxOf(
                                editor.document.getLineStartOffset(boundedLine),
                                editor.document.getLineStartOffset(boundedLine) + column - 1
                            ),
                            editor.document.getLineEndOffset(boundedLine)
                        )
                    } else {
                        editor.document.getLineStartOffset(boundedLine)
                    }

                if (!editor.selectionModel.hasSelection()) {
                    MarkHandler.pushPlaceInfo(editor)
                }
                editor.caretModel.primaryCaret.moveToOffset(position)
                editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            }
            hide()
        }
    }

    internal fun hide() {
        Disposer.dispose(this)
    }

    override fun dispose() {
        if (!isDisposed) { // ui.cancelUI() below re-enters through the popup's cancel callback.
            isDisposed = true

            ui.cancelUI()

            GotoLineHandler.delegate = null
        }
    }
}
