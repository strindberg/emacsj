package com.github.strindberg.emacsj.macro

import kotlin.concurrent.thread
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.github.strindberg.emacsj.universal.universalCommandNames
import com.intellij.ide.actionMacro.ActionMacroManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_RUN_LAST_MACRO = "com.github.strindberg.emacsj.actions.macro.runlastmacro"

class RunLastMacroHandler : EditorWriteActionHandler() {

    @Suppress("UnusedPrivateProperty", "LoopWithTooManyJumpStatements")
    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val macroManager = ActionMacroManager.getInstance()
        if (macroManager.hasRecentMacro()) {
            val times =
                if (EmacsJCommandListener.lastCommandName in universalCommandNames) UniversalArgumentHandler.lastArgument else 1
            WriteAction.run<Throwable> {
                thread {
                    UniversalArgumentHandler.repeating = true
                    for (i in 0..<times) {
                        if (UniversalArgumentHandler.cancel) {
                            break
                        }
                        macroManager.playbackLastMacro()
                        do {
                            Thread.sleep(10)
                        } while (macroManager.isPlaying)
                    }
                    UniversalArgumentHandler.repeating = false
                }
            }
        }
    }
}
