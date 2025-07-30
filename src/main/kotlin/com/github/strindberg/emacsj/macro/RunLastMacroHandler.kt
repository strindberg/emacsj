package com.github.strindberg.emacsj.macro

import java.time.LocalTime
import kotlin.concurrent.thread
import com.github.strindberg.emacsj.EmacsJBundle
import com.github.strindberg.emacsj.EmacsJCommandListener
import com.github.strindberg.emacsj.preferences.EmacsJSettings
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT0
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT1
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT2
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT3
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT4
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT5
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT6
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT7
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT8
import com.github.strindberg.emacsj.universal.ACTION_UNIVERSAL_ARGUMENT9
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.ide.actionMacro.ActionMacroManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.ui.Messages
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_RUN_LAST_MACRO = "com.github.strindberg.emacsj.actions.macro.runlastmacro"

class RunLastMacroHandler : EditorWriteActionHandler() {

    private val universalCommands = listOf(
        ACTION_UNIVERSAL_ARGUMENT,
        ACTION_UNIVERSAL_ARGUMENT1,
        ACTION_UNIVERSAL_ARGUMENT2,
        ACTION_UNIVERSAL_ARGUMENT3,
        ACTION_UNIVERSAL_ARGUMENT4,
        ACTION_UNIVERSAL_ARGUMENT5,
        ACTION_UNIVERSAL_ARGUMENT6,
        ACTION_UNIVERSAL_ARGUMENT7,
        ACTION_UNIVERSAL_ARGUMENT8,
        ACTION_UNIVERSAL_ARGUMENT9,
        ACTION_UNIVERSAL_ARGUMENT0,
    ).map { EmacsJBundle.actionText(it) }

    @Suppress("UnusedPrivateProperty", "LoopWithTooManyJumpStatements")
    override fun executeWriteAction(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val macroManager = ActionMacroManager.getInstance()
        if (macroManager.hasRecentMacro()) {
            val times = if (EmacsJCommandListener.lastCommandName in universalCommands) UniversalArgumentHandler.lastCount else 1
            val timeout = EmacsJSettings.getInstance().state.repeatedMacroTimeoutSeconds
            WriteAction.run<Throwable> {
                thread {
                    UniversalArgumentHandler.repeating = true
                    val start = LocalTime.now()
                    for (i in 0..<times) {
                        if (UniversalArgumentHandler.cancel) {
                            break
                        }
                        if (LocalTime.now().isAfter(start.plusSeconds(timeout))) {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showInfoMessage(
                                    editor.project,
                                    "Repeating the macro was aborted after the configured timeout.\n\n" +
                                        "To change the repeat timeout:\n'Settings → Editor → EmacsJ'\n\n" +
                                        "To change the macro execution speed:\n'Registry → actionSystem.playback.typecommand.delay'",
                                    "Timeout",
                                )
                            }
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
