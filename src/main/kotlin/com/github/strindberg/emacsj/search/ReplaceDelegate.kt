package com.github.strindberg.emacsj.search

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.util.UUID
import com.github.strindberg.emacsj.search.ReplaceHandler.Companion.addPrevious
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.view.ACTION_RECENTER
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.colors.EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

internal class ReplaceDelegate(val editor: Editor, val type: SearchType, val selection: IntRange?, lastSearch: Replace?) {

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            ui.cancelUI()
        }
    }

    private var searchArg: String = ""

    private var replaceArg: String = ""

    private lateinit var replaceModel: FindModel

    private lateinit var lastResult: FindResult

    private lateinit var undoGroupId: String

    private var replaced = 0

    private val replacements = ArrayDeque<Replaced>()

    private val identifierAttributes: TextAttributes

    private var state: ReplaceState = ReplaceState.GET_SEARCH_ARG
        set(state) {
            field = state
            ui.title = getReplaceTitle()
        }

    private var inhibitCancel = false

    private var isReplaced = false

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    @VisibleForTesting
    internal val ui = CommonUI(editor, true, ::hide, ::keyEventHandler)

    init {
        lastSearch?.let { (search, replace) ->
            searchArg = search
            replaceArg = replace
            ui.text = getReplaceChoiceText()
            ui.selectText()
        }

        ui.title = getReplaceTitle()

        editor.caretModel.removeSecondaryCarets()
        editor.caretModel.addCaretListener(caretListener)

        identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)
        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, ERASE_MARKER)

        ui.show()
    }

    internal fun hide() {
        if (!inhibitCancel) {
            editor.markupModel.removeAllHighlighters()

            editor.caretModel.removeCaretListener(caretListener)

            editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

            ui.cancelUI()

            ReplaceHandler.delegate = null
        }
    }

    internal fun addNewLine() {
        if (state in listOf(ReplaceState.GET_SEARCH_ARG, ReplaceState.GET_REPLACE_ARG)) {
            text += "\n"
        }
    }

    internal fun setTextFromPrevious(previous: Replace) {
        if (state == ReplaceState.GET_SEARCH_ARG) {
            text = previous.search
        } else if (state == ReplaceState.GET_REPLACE_ARG) {
            text = previous.replace
        }
    }

    private fun keyEventHandler(e: KeyEvent) {
        when (state) {
            ReplaceState.GET_SEARCH_ARG -> {
                if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED && e.modifiersEx == 0) {
                    editor.markupModel.removeAllHighlighters()
                    val matchResult = Regex("(^.*) -> (.*)$", RegexOption.DOT_MATCHES_ALL).matchEntire(ui.text)?.destructured
                    if (matchResult != null) {
                        searchArg = matchResult.component1()
                        replaceArg = matchResult.component2()
                        startSearch()
                    } else {
                        state = ReplaceState.GET_REPLACE_ARG
                        searchArg = ui.text
                        ui.text = ""
                        ReplaceHandler.resetPos()
                    }
                } else if (e.id == KeyEvent.KEY_RELEASED) {
                    editor.markupModel.removeAllHighlighters()
                    CommonHighlighter.findAllAndHighlight(
                        editor = editor,
                        searchArg = ui.text,
                        useRegexp = type == REGEXP,
                        useCase = type == REGEXP || caseSensitive(ui.text),
                        range = selection
                    )
                }
            }

            ReplaceState.GET_REPLACE_ARG -> {
                if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED) {
                    replaceArg = ui.text
                    startSearch()
                }
            }

            ReplaceState.EDIT_REPLACE_ARG -> {
                if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED) {
                    replaceArg = ui.text
                    startEditedSearch()
                }
            }

            ReplaceState.SEARCHING -> {
            }

            ReplaceState.SEARCH_FOUND -> {
                if (e.id == KeyEvent.KEY_TYPED) {
                    when (e.keyChar.lowercaseChar()) {
                        '\u000c' -> { // Ctrl-L
                            val recenterAction = ActionManager.getInstance().getAction(ACTION_RECENTER)
                            ActionUtil.invokeAction(recenterAction, editor.component, "Recenter", null, null)
                        }
                        'y', ' ' -> {
                            try {
                                replaceInEditor()
                                searchForReplacement(true)
                            } catch (e: FindManager.MalformedReplacementStringException) {
                                handleReplacementError(e)
                            }
                        }
                        ',' -> {
                            try {
                                replaceInEditor()
                                isReplaced = true
                            } catch (e: FindManager.MalformedReplacementStringException) {
                                handleReplacementError(e)
                            }
                        }
                        'n' -> {
                            searchForReplacement(true)
                        }
                        'e' -> {
                            state = ReplaceState.EDIT_REPLACE_ARG
                            inhibitCancel = true
                            ui.makeWriteable(replaceArg)
                            inhibitCancel = false
                        }
                        '.' -> {
                            try {
                                replaceInEditor()
                            } catch (e: FindManager.MalformedReplacementStringException) {
                                handleReplacementError(e)
                            }
                            ui.cancelUI()
                        }
                        '!' -> {
                            try {
                                do {
                                    replaceInEditor()
                                    searchForReplacement(false)
                                } while (lastResult.isStringFound)
                            } catch (e: FindManager.MalformedReplacementStringException) {
                                handleReplacementError(e)
                            }
                            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
                        }
                        'u' -> {
                            val lastReplacement = replacements.removeLastOrNull()
                            if (lastReplacement != null) {
                                undoReplacement(lastReplacement)
                            } else {
                                ui.flashText("Nothing to undo")
                            }
                        }
                        '^' -> {
                            val lastReplacement = replacements.removeLastOrNull()
                            if (lastReplacement != null) {
                                visitReplacement(lastReplacement)
                                isReplaced = true
                            } else {
                                ui.flashText("No previous match")
                            }
                        }
                        else -> {
                            ui.cancelUI()
                        }
                    }
                }
            }

            ReplaceState.REPLACE_DONE, ReplaceState.REPLACE_FAILED -> {
                if (e.id == KeyEvent.KEY_PRESSED) {
                    ui.cancelUI()
                }
            }
        }
    }

    private fun startSearch() {
        state = ReplaceState.SEARCHING

        addPrevious(searchArg, replaceArg, type)

        ui.makeReadonly(getReplaceChoiceText(), true)
        setupModel()

        editor.selectionModel.removeSelection()

        searchForReplacement(true)
    }

    private fun startEditedSearch() {
        state = ReplaceState.SEARCHING

        addPrevious(searchArg, replaceArg, type)

        ui.makeReadonly(getReplaceChoiceText(), true)
        setupModel()

        try {
            replaceInEditor()
            searchForReplacement(true)
        } catch (e: FindManager.MalformedReplacementStringException) {
            handleReplacementError(e)
        }
    }

    private fun setupModel() {
        replaceModel = FindModel().apply {
            stringToFind = searchArg
            isForward = true
            isRegularExpressions = type == REGEXP
            isCaseSensitive = type == REGEXP || caseSensitive(searchArg) || caseSensitive(replaceArg)
            isPreserveCase = !(type == REGEXP || caseSensitive(searchArg) || caseSensitive(replaceArg))
            stringToReplace = fixBackReferences(replaceArg)
        }
        undoGroupId = UUID.randomUUID().toString()
    }

    private fun fixBackReferences(replaceArgument: String): String {
        var argument = replaceArgument
        val matches = Regex("""(\\\\[0-9]+|\\\\&)""").findAll(argument).map { result ->
            argument = argument.replaceRange(result.range, "A".repeat(result.range.last - result.range.first))
            Pair(result.range, result.value)
        }
        argument = argument.replace(Regex("""\\([0-9])"""), """\$$1""").replace(Regex("""\\&"""), """\$0""")
        matches.forEach { (range, content) ->
            argument = argument.replaceRange(range.first, range.last, content)
        }
        return argument
    }

    private fun handleReplacementError(e: FindManager.MalformedReplacementStringException) {
        thisLogger().warn(e.message)
        ui.textColor = JBColor.RED
        editor.markupModel.removeAllHighlighters()
        state = ReplaceState.REPLACE_FAILED
    }

    private fun getReplaceTitle() =
        when (state) {
            ReplaceState.GET_SEARCH_ARG -> if (type == REGEXP) "Query replace regexp: " else "Query replace: "
            ReplaceState.GET_REPLACE_ARG, ReplaceState.SEARCHING, ReplaceState.EDIT_REPLACE_ARG -> "Replace with: "
            ReplaceState.SEARCH_FOUND -> "Replace? "
            ReplaceState.REPLACE_DONE -> if (replaced == 1) "Replaced 1 occurrence." else "Replaced $replaced occurrences."
            ReplaceState.REPLACE_FAILED -> "Replacement failed. "
        }

    private fun getReplaceChoiceText(): String = "$searchArg -> $replaceArg"

    private fun searchForReplacement(highlight: Boolean) {
        val result = if (selection != null) {
            FindManager.getInstance(editor.project).findString(editor.text.substring(0, selection.last), selection.first, replaceModel)
        } else {
            FindManager.getInstance(editor.project).findString(editor.text, editor.caretModel.offset, replaceModel)
        }

        if (result.isStringFound) {
            isReplaced = false
            editor.caretModel.moveToOffset(result.endOffset)

            if (highlight) {
                highlight(result.startOffset, result.endOffset)
                editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            }

            state = ReplaceState.SEARCH_FOUND
        } else {
            ui.text = ""
            state = ReplaceState.REPLACE_DONE
        }

        lastResult = result
    }

    private fun replaceInEditor() {
        if (!isReplaced) {
            val foundString = editor.document.substring(lastResult.startOffset, lastResult.endOffset)
            val replacement = FindManager.getInstance(editor.project)
                .getStringToReplace(foundString, replaceModel, lastResult.startOffset, editor.text)

            WriteCommandAction.runWriteCommandAction(editor.project, "Replace ${type.name.lowercase()}", undoGroupId, {
                editor.document.replaceString(lastResult.startOffset, lastResult.endOffset, replacement)
            })

            replacements.addLast(Replaced(lastResult.startOffset, foundString, replacement))

            editor.caretModel.moveToOffset(lastResult.startOffset + replacement.length)

            replaced++
        }
    }

    private fun undoReplacement(item: Replaced) {
        WriteCommandAction.runWriteCommandAction(editor.project, "Undo replace ${type.name.lowercase()}", undoGroupId, {
            editor.document.replaceString(item.startOffset, item.endOffset, item.original)
        })

        editor.caretModel.moveToOffset(item.startOffset)
        editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)

        replaced--

        searchForReplacement(true)
    }

    private fun visitReplacement(item: Replaced) {
        editor.caretModel.moveToOffset(item.endOffset)
        editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)

        highlight(item.startOffset, item.endOffset)
    }

    private fun highlight(startOffset: Int, endOffset: Int) {
        editor.markupModel.removeAllHighlighters()

        editor.markupModel.addRangeHighlighter(
            EMACSJ_PRIMARY,
            startOffset,
            endOffset,
            HighlighterLayer.LAST + 2,
            HighlighterTargetArea.EXACT_RANGE
        )

        CommonHighlighter.findAllAndHighlight(
            editor = editor,
            searchArg = searchArg,
            useRegexp = type == REGEXP,
            useCase = replaceModel.isCaseSensitive,
            range = selection
        )
    }
}

internal enum class ReplaceState {
    GET_SEARCH_ARG,
    GET_REPLACE_ARG,
    SEARCHING,
    SEARCH_FOUND,
    REPLACE_DONE,
    REPLACE_FAILED,
    EDIT_REPLACE_ARG,
}

internal data class Replace(val search: String, val replace: String) {
    companion object {
        val EMPTY = Replace("", "")
    }
}

internal data class Replaced(val startOffset: Int, val original: String, val replacement: String) {
    val endOffset = startOffset + replacement.length
}
