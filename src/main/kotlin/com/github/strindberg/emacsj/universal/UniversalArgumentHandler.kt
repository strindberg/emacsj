package com.github.strindberg.emacsj.universal

import java.util.UUID
import com.github.strindberg.emacsj.EmacsJScope
import com.github.strindberg.emacsj.EmacsJService
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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

internal val universalActionIds = [
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
]

private const val BATCH_SIZE = 100

internal class UniversalArgumentHandler(private val numeric: Int?) : EditorActionHandler() {

    companion object {
        internal var delegate: UniversalArgumentDelegate? = null

        private var repeatJob: Job? = null

        /** Runs [action] [times] times, off the delegate that asked for it. */
        @Suppress("TooGenericExceptionCaught")
        internal fun startRepeat(project: Project?, times: Int, action: () -> Unit) {
            val groupId = UUID.randomUUID().toString()

            val previous = repeatJob
            EmacsJService.instance.setRepeating(true)
            repeatJob = EmacsJScope.instance.scope.launch(Dispatchers.EDT) {
                try {
                    // A repeat queued while another is still running waits for it, rather than interleaving with it.
                    previous?.join()
                    repeat(times) { index ->
                        CommandProcessor.getInstance().executeCommand(project, action, null, groupId)
                        // Hand the EDT back every batch, so a long repeat stays interruptible.
                        if ((index + 1) % BATCH_SIZE == 0) {
                            yield()
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    thisLogger().warn(e)
                } finally {
                    // Only if no newer repeat has taken over; clearing then would cancel that one instead.
                    if (coroutineContext.job === repeatJob) {
                        repeatJob = null
                        EmacsJService.instance.setRepeating(false)
                    }
                }
            }
        }

        /** Drops the repetitions of a running repeat that have not run yet. */
        internal fun cancelRepeat() {
            repeatJob?.cancel()
            repeatJob = null
            EmacsJService.instance.setRepeating(false)
        }
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (numeric == null) {
                current.multiply()
            } else {
                current.addDigit(numeric)
            }
            EmacsJService.instance.registerUniversalArgument(current.getTimes())
        } else {
            val newDelegate = UniversalArgumentDelegate(editor = editor, numeric = numeric, caret = caret, dataContext = dataContext)
            delegate = newDelegate
            EmacsJService.instance.registerUniversalArgument(newDelegate.getTimes())
        }
    }
}
