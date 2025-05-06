package com.github.strindberg.emacsj.zap

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

enum class ZapType { FORWARD_TO, FORWARD_UP_TO, BACKWARD_TO, BACKWARD_UP_TO }

@Language("devkit-action-id")
internal const val ACTION_ZAP_FORWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_FORWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapupto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_BACKWARD_TO = "com.github.strindberg.emacsj.actions.zap.zapbackto"

@Language("devkit-action-id")
internal const val ACTION_ZAP_BACKWARD_UP_TO = "com.github.strindberg.emacsj.actions.zap.zapbackupto"

class ZapHandler(private val type: ZapType) : EditorActionHandler() {

    companion object {
        internal var delegate: ZapDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        delegate = ZapDelegate(editor, type)
    }
}
