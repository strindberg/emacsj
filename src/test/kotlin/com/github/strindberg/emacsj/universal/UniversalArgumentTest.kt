package com.github.strindberg.emacsj.universal

import java.awt.event.KeyEvent.VK_ESCAPE
import kotlin.time.Duration
import kotlin.time.TestTimeSource
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.EmacsJTestCase
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_DELETE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_LEFT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_MOVE_CARET_RIGHT
import com.intellij.openapi.actionSystem.IdeActions.ACTION_UNDO
import com.intellij.testFramework.PlatformTestUtil
import kotlinx.coroutines.yield
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val FILE = "universalfile.txt"

/** How many repetitions make up a batch under [TickingTimeSource]. */
private const val REPETITIONS_PER_BATCH = 100

class UniversalArgumentTest : EmacsJTestCase() {

    @BeforeEach
    fun fixBatchSize() {
        UniversalArgumentHandler.timeSource = TickingTimeSource(REPEAT_BATCH_DURATION / REPETITIONS_PER_BATCH)
    }

    @AfterEach
    fun restoreTimeSource() {
        UniversalArgumentHandler.timeSource = TimeSource.Monotonic
    }

    @Test
    fun `Universal argument before movement moves four steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("foob<caret>ar")
    }

    @Test
    fun `Universal argument with '5' before movement moves five steps`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("fooba<caret>r")
    }

    @Test
    fun `First non-digit after Universal argument triggers action`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("5")
        myFixture.type("a")
        checkResult("aaaaa<caret>")
    }

    @Test
    fun `Multiple digits are interpreted as number`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("1")
        myFixture.type("5")
        myFixture.type("a")
        checkResult("aaaaaaaaaaaaaaa<caret>")
    }

    @Test
    fun `Repeated Universal argument multiplies by four`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("a")
        checkResult("aaaaaaaaaaaaaaaa<caret>")
    }

    @Test
    fun `Pressing 'Escape' aborts universal argument`() {
        myFixture.configureByText(FILE, "<caret>foobar")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        pressEscape()
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)
        checkResult("f<caret>oobar")
    }

    @Test
    fun `Numeric universal arguments work`() {
        [
            ACTION_UNIVERSAL_ARGUMENT1 to 1,
            ACTION_UNIVERSAL_ARGUMENT2 to 2,
            ACTION_UNIVERSAL_ARGUMENT3 to 3,
            ACTION_UNIVERSAL_ARGUMENT4 to 4,
            ACTION_UNIVERSAL_ARGUMENT5 to 5,
            ACTION_UNIVERSAL_ARGUMENT6 to 6,
            ACTION_UNIVERSAL_ARGUMENT7 to 7,
            ACTION_UNIVERSAL_ARGUMENT8 to 8,
            ACTION_UNIVERSAL_ARGUMENT9 to 9
        ].forEach { (action, times) ->
            myFixture.configureByText(FILE, "<caret>")

            myFixture.performEditorAction(action)
            myFixture.type("a")
            checkResult("a".repeat(times) + "<caret>")

            myFixture.performEditorAction(action)
            myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
            checkResult("<caret>" + "a".repeat(times))

            UniversalArgumentHandler.delegate?.hide()
        }
    }

    @Test
    fun `Numeric universal argument 10 works 1`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("0")
        myFixture.type("a")
        checkResult("aaaaaaaaaa<caret>")
    }

    @Test
    fun `Numeric universal argument 10 works 2`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.type("a")
        checkResult("aaaaaaaaaa<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT0)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_LEFT)
        checkResult("<caret>aaaaaaaaaa")
    }

    @Test
    fun `A repeat larger than the batch size runs every repetition`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("5")
        myFixture.type("0")
        myFixture.type("a")
        checkResult("a".repeat(150) + "<caret>")
    }

    @Test
    fun `Repeating is switched off once the repeat has finished`() {
        myFixture.configureByText(FILE, "<caret>")
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT5)
        myFixture.type("a")

        runPendingRepeats()

        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `Cancelling the repeat drops repetitions that have not run yet`() {
        myFixture.configureByText(FILE, "<caret>")
        // The first batch runs before the keystroke returns; only what comes after it can still be dropped.
        repeatTimes150("a")

        myFixture.performEditorAction(ACTION_CANCEL_REPEAT)

        checkResult("a".repeat(100) + "<caret>")
    }

    @Test
    fun `Universal argument repeat applies to every caret`() {
        myFixture.configureByText(
            FILE,
            """
                |<caret>abcdef
                |<caret>abcdef
            """.trimMargin()
        )

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT3)
        myFixture.performEditorAction(ACTION_EDITOR_MOVE_CARET_RIGHT)

        checkResult(
            """
                |abc<caret>def
                |abc<caret>def
            """.trimMargin()
        )
    }

    @Test
    fun `Universal argument before backspace deletes four characters`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)

        checkResult("abcd<caret>ijkl")
    }

    @Test
    fun `Universal argument before delete deletes four characters`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_DELETE)

        checkResult("abcdefgh<caret>")
    }

    @Test
    fun `A repeated deletion is undone in one step`() {
        myFixture.configureByText(FILE, "abcdefgh<caret>ijkl")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.performEditorAction(ACTION_EDITOR_BACKSPACE)
        checkResult("abcd<caret>ijkl")

        // The repetitions share one command group id, so they collapse into the single step the first press began.
        myFixture.performEditorAction(ACTION_UNDO)
        checkResult("abcdefghijkl")
    }

    @Test
    fun `A repeat queued while another is still pending runs after it, not interleaved with it`() {
        myFixture.configureByText(FILE, "<caret>")

        // More than one batch each, so that the two repeats would interleave at their yield points if the second
        // one did not wait for the first.
        repeatTimes150("a")
        repeatTimes150("b")

        checkResult("a".repeat(150) + "b".repeat(150) + "<caret>")
    }

    @Test
    fun `A launched repeat holds the repeat flag until it has run`() {
        var hasLaunchedRun = false

        // Suspends at once, as a repeat does at the end of its first batch.
        UniversalArgumentHandler.launchRepeat {
            yield()
            hasLaunchedRun = true
        }

        assertTrue(EmacsJService.instance.isRepeating())
        runPendingRepeats()
        assertTrue(hasLaunchedRun)
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `Cancelling the repeat stops a launched repeat`() {
        var hasLaunchedRun = false

        UniversalArgumentHandler.launchRepeat {
            yield()
            hasLaunchedRun = true
        }
        UniversalArgumentHandler.cancelRepeat()
        runPendingRepeats()

        assertFalse(hasLaunchedRun)
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `A repeat played by a macro runs where it was recorded`() {
        myFixture.configureByText(FILE, "<caret>")

        UniversalArgumentHandler.launchMacroRepeat {
            myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
            myFixture.type("3")
            myFixture.type("a")
            // Macro playback is asynchronous, handing the EDT back between steps.
            repeat(3) { yield() }
            myFixture.type("b")
        }

        checkResult("aaab<caret>")
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `A macro repeat holds the repeat flag while a repeat it played finishes`() {
        var hasPlayedRepeatRun = false
        var isRepeatingAfterPlayedRepeat = false

        UniversalArgumentHandler.launchMacroRepeat {
            // Suspends, so that it is still live while the macro repeat goes on.
            UniversalArgumentHandler.launchRepeat {
                yield()
                hasPlayedRepeatRun = true
            }
            repeat(3) { yield() }
            isRepeatingAfterPlayedRepeat = EmacsJService.instance.isRepeating()
        }

        runPendingRepeats()
        assertTrue(hasPlayedRepeatRun)
        assertTrue(isRepeatingAfterPlayedRepeat)
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `Cancelling the repeat stops a macro repeat`() {
        var hasLaunchedRun = false

        UniversalArgumentHandler.launchMacroRepeat { hasLaunchedRun = true }
        UniversalArgumentHandler.cancelRepeat()
        runPendingRepeats()

        assertFalse(hasLaunchedRun)
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `A repeat triggered by typing runs before the next typed character`() {
        myFixture.configureByText(FILE, "<caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("3")
        // Macro playback types a recorded string in one go, without handing the EDT back between its characters.
        myFixture.type("b c")

        checkResult("bbb c<caret>")
    }

    @Test
    fun `A repeat of a typed character is undone in one step`() {
        myFixture.configureByText(FILE, "foo <caret>")

        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT)
        myFixture.type("3")
        myFixture.type("b")
        checkResult("foo bbb<caret>")

        myFixture.performEditorAction(ACTION_UNDO)
        checkResult("foo <caret>")
    }

    @Test
    fun `A repeat of a typed character running past one batch is undone in one step`() {
        myFixture.configureByText(FILE, "<caret>")

        repeatTimes150("a")
        checkResult("a".repeat(150) + "<caret>")

        myFixture.performEditorAction(ACTION_UNDO)
        checkResult("<caret>")
    }

    @Test
    fun `A repeat with nothing queued ahead of it runs at once and leaves no repeat behind`() {
        var hasLaunchedRun = false

        UniversalArgumentHandler.launchRepeat { hasLaunchedRun = true }

        assertTrue(hasLaunchedRun)
        assertFalse(EmacsJService.instance.isRepeating())
    }

    @Test
    fun `Cheap repetitions past a hundred still run before the next typed character`() {
        // Time stands still, so the whole repeat fits in its first batch, as a fast one does in the IDE.
        UniversalArgumentHandler.timeSource = TestTimeSource()
        myFixture.configureByText(FILE, "<caret>")

        repeatTimes150("b")
        // Typed in one go, as macro playback does.
        myFixture.type(" c")

        checkResult("b".repeat(150) + " c<caret>")
    }

    /** Only a repeat's first batch runs inline; let the rest, and any repeat queued behind another, finish before asserting. */
    private fun checkResult(expected: String) {
        runPendingRepeats()
        myFixture.checkResult(expected)
    }

    /**
     * Runs whatever the universal-argument machinery has queued. A repeat runs in a coroutine on the EDT that yields
     * every batch, so that a long repeat stays interruptible: past its first batch, or when queued behind another
     * repeat, it has not finished by the time the triggering action returns.
     */
    private fun runPendingRepeats() {
        PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }

    private fun repeatTimes150(text: String) {
        myFixture.performEditorAction(ACTION_UNIVERSAL_ARGUMENT1)
        myFixture.type("5")
        myFixture.type("0")
        myFixture.type(text)
    }

    private fun pressEscape() {
        pressKey(UniversalArgumentHandler.delegate?.ui, VK_ESCAPE)
        UniversalArgumentHandler.delegate?.hide()
    }
}

/**
 * Advances by [tick] every time a mark is read, so that a batch ends after a fixed number of repetitions, however fast
 * the machine running the test is.
 */
private class TickingTimeSource(private val tick: Duration) : TimeSource {

    override fun markNow(): TimeMark =
        object : TimeMark {
            private var elapsed = Duration.ZERO

            override fun elapsedNow(): Duration {
                elapsed += tick
                return elapsed
            }
        }
}
