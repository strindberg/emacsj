package com.github.strindberg.emacsj.macro

import kotlin.time.Duration.Companion.milliseconds
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.ActionWrapperUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language

@Language("devkit-action-id")
internal const val ACTION_RUN_LAST_MACRO = "com.github.strindberg.emacsj.actions.macro.runlastmacro"

@Language("devkit-action-id")
private const val ACTION_PLAYBACK_LAST_MACRO = "PlaybackLastMacro"

private val PLAYBACK_POLL = 10.milliseconds

/**
 * Runs the last recorded macro, [EmacsJService.universalArgument] times.
 *
 * Playback is asynchronous, so the runs have to be sequenced. The platform tracks this in `ActionMacroManager.isPlaying`,
 * which is internal, but `PlaybackLastMacro` reports it: that action is enabled exactly when a macro exists and none is
 * playing. Polling its presentation is therefore an internal-API-free way of waiting for a run to complete.
 */
internal class RunLastMacroHandler : EditorActionHandler() {

    override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean =
        delegate()?.isEnabledIn(editor) == true

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
        delegate()?.let { delegate ->
            val times = EmacsJService.instance.universalArgument()

            // Playback returns before the macro has run, so each run is awaited before the next one starts. The wait
            // suspends, which also keeps the repeat cancellable.
            UniversalArgumentHandler.launchMacroRepeat {
                var remaining = times
                while (remaining > 0 && !editor.isDisposed) {
                    ActionWrapperUtil.actionPerformed(delegate.event(editor), wrapper(), delegate)
                    delegate.awaitPlaybackFinished(editor)
                    remaining--
                }
            }
        }
    }

    private fun delegate(): AnAction? = ActionManager.getInstance().getAction(ACTION_PLAYBACK_LAST_MACRO)

    private fun wrapper(): AnAction = ActionManager.getInstance().getAction(ACTION_RUN_LAST_MACRO)
}

/**
 * Whether the action reports itself as enabled in [editor].
 *
 * [AnAction.update] is `@ApiStatus.OverrideOnly` - the platform reserves the call for itself - so it is never invoked
 * directly here; [ActionUtil] makes the call.
 */
private fun AnAction.isEnabledIn(editor: Editor): Boolean {
    val event = event(editor)
    ActionUtil.updateAction(this, event)
    return event.presentation.isEnabled
}

private suspend fun AnAction.awaitPlaybackFinished(editor: Editor) {
    // The delegate stays disabled for as long as a macro is playing, but also if the last macro is removed meanwhile.
    // Such a wait never ends on its own; delay() is where cancelling the repeat breaks it.
    while (!isEnabledIn(editor) && !editor.isDisposed) {
        delay(PLAYBACK_POLL)
    }
}

private fun AnAction.event(editor: Editor): AnActionEvent =
    AnActionEvent.createEvent(
        this,
        DataManager.getInstance().getDataContext(editor.contentComponent),
        null,
        ActionPlaces.KEYBOARD_SHORTCUT,
        ActionUiKind.NONE,
        null,
    )
