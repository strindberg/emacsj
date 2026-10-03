package com.github.strindberg.emacsj.universal

import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import com.github.strindberg.emacsj.EmacsJScope
import com.github.strindberg.emacsj.EmacsJService
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting

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

internal val REPEAT_BATCH_DURATION = 200.milliseconds

internal class UniversalArgumentHandler(private val numeric: Int?) : EditorActionHandler() {

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        val current = delegate
        if (current != null) {
            if (numeric == null) {
                current.multiply()
            } else {
                current.addDigit(numeric)
            }
        } else {
            delegate = UniversalArgumentDelegate(editor = editor, numeric = numeric, caret = caret, dataContext = dataContext)
        }
    }

    companion object {
        internal var delegate: UniversalArgumentDelegate? = null

        @VisibleForTesting
        internal var timeSource: TimeSource = TimeSource.Monotonic

        private var repeatJob: Job? = null

        private var macroJob: Job? = null

        internal fun startRepeat(project: Project?, times: Int, action: () -> Unit) {
            val groupId = UUID.randomUUID().toString()

            launchRepeat {
                var batch = timeSource.markNow()
                repeat(times) {
                    CommandProcessor.getInstance().executeCommand(project, action, null, groupId)
                    // Hand the EDT back every batch, so a long repeat stays interruptible.
                    if (batch.elapsedNow() >= REPEAT_BATCH_DURATION) {
                        yield()
                        batch = timeSource.markNow()
                    }
                }
            }
        }

        internal fun launchRepeat(block: suspend () -> Unit) {
            val start = if (repeatJob == null && ApplicationManager.getApplication().isDispatchThread) UNDISPATCHED else DEFAULT
            val job = launchAfter(listOfNotNull(repeatJob), start, block) { job -> if (job === repeatJob) repeatJob = null }
            if (!job.isCompleted) {
                repeatJob = job
            }
        }

        internal fun launchMacroRepeat(block: suspend () -> Unit) {
            macroJob = launchAfter(listOfNotNull(repeatJob, macroJob), DEFAULT, block) { job -> if (job === macroJob) macroJob = null }
        }

        internal fun cancelRepeat() {
            repeatJob?.cancel()
            repeatJob = null
            macroJob?.cancel()
            macroJob = null
            EmacsJService.instance.setRepeating(false)
        }

        @Suppress("TooGenericExceptionCaught")
        private fun launchAfter(previous: List<Job>, start: CoroutineStart, block: suspend () -> Unit, release: (Job) -> Unit): Job {
            EmacsJService.instance.setRepeating(true)
            return EmacsJScope.instance.scope.launch(Dispatchers.EDT, start) {
                try {
                    previous.joinAll()
                    block()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    thisLogger().warn(e)
                } finally {
                    release(coroutineContext.job)
                    EmacsJService.instance.setRepeating(repeatJob != null || macroJob != null)
                }
            }
        }
    }
}
