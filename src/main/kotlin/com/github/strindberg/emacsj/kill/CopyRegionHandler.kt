package com.github.strindberg.emacsj.kill

import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.util.DocumentUtil
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting

@Language("devkit-action-id")
internal const val ACTION_COPY = "com.github.strindberg.emacsj.actions.kill.copy"

class CopyRegionHandler : EditorActionHandler() {

    companion object {
        @VisibleForTesting
        internal var isTesting = false
    }

    private var lastInvocation = OffsetDateTime.MIN

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val primary = caret ?: editor.caretModel.primaryCaret
        if (editor.selectionModel.hasSelection()) {
            KillUtil.copy(
                editor = editor,
                textStartOffset = editor.selectionModel.selectionStart,
                textEndOffset = editor.selectionModel.selectionEnd,
                prepend = primary.offset == editor.selectionModel.selectionStart,
            )
        } else if (throttled()) {
            KillUtil.copy(
                editor = editor,
                textStartOffset = DocumentUtil.getLineStartOffset(primary.offset, editor.document),
                textEndOffset = minOf(editor.document.textLength, DocumentUtil.getLineEndOffset(primary.offset, editor.document) + 1),
            )
        }
        lastInvocation = now()
    }

    // Avoid inadvertently running the command multiple times because of key repeat.
    private fun throttled(): Boolean = isTesting || lastInvocation.isBefore(now().minus(200.milliseconds.toJavaDuration()))
}
