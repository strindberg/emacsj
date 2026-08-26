package com.github.strindberg.emacsj.zap

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class ZapType(val description: String) {
    FORWARD_TO("Zap to Character"),
    FORWARD_UP_TO("Zap up to Character"),
    BACKWARD_TO("Zap Back to Character"),
    BACKWARD_UP_TO("Zap Back up to Character")
}

@Language("devkit-action-id")
internal const val ACTION_ZAP_FORWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_FORWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapupto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_BACKWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapbackto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_BACKWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapbackupto"

internal val zapActionIds = [ACTION_ZAP_FORWARD_TO, ACTION_ZAP_FORWARD_UP_TO, ACTION_ZAP_BACKWARD_TO, ACTION_ZAP_BACKWARD_UP_TO]

internal class ZapHandler(private val type: ZapType) : EditorActionHandler() {

    companion object {
        internal var delegate: ZapDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        delegate = ZapDelegate(editor, type)
    }
}
