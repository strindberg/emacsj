package com.github.strindberg.emacsj.universal

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT = "com.github.strindberg.emacsj.actions.universal.universalargument"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT1 = "com.github.strindberg.emacsj.actions.universal.universalargument1"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT2 = "com.github.strindberg.emacsj.actions.universal.universalargument2"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT3 = "com.github.strindberg.emacsj.actions.universal.universalargument3"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT4 = "com.github.strindberg.emacsj.actions.universal.universalargument4"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT5 = "com.github.strindberg.emacsj.actions.universal.universalargument5"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT6 = "com.github.strindberg.emacsj.actions.universal.universalargument6"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT7 = "com.github.strindberg.emacsj.actions.universal.universalargument7"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT8 = "com.github.strindberg.emacsj.actions.universal.universalargument8"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT9 = "com.github.strindberg.emacsj.actions.universal.universalargument9"

@Language("devkit-action-id")
internal const val ACTION_UNIVERSAL_ARGUMENT0 = "com.github.strindberg.emacsj.actions.universal.universalargument0"

class UniversalArgumentHandler(val numeric: Int?) : EditorActionHandler() {

    companion object {
        internal var delegate: UniversalArgumentDelegate? = null
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (numeric == null) {
                current.multiply()
            } else {
                current.addDigit(numeric)
            }
        } else {
            delegate = UniversalArgumentDelegate(editor, dataContext, numeric)
        }
    }
}
