package com.github.strindberg.emacsj.search

import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.util.UUID
import com.github.strindberg.emacsj.search.ReplaceHandler.Companion.addPrevious
import com.github.strindberg.emacsj.search.ReplaceState.GET_REPLACE_ARG
import com.github.strindberg.emacsj.search.ReplaceState.GET_SEARCH_ARG
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.ui.UIDelegate
import com.github.strindberg.emacsj.view.ACTION_RECENTER
import com.github.strindberg.emacsj.word.substring
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.colors.EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

internal class ReplaceDelegate(
    editor: Editor,
    private val project: Project,
    val type: SearchType,
    private val selection: IntRange? = null,
    lastSearch: Replace? = null,
) : UIDelegate(editor) {

    @VisibleForTesting
    override val ui = CommonUI(editor = editor, isWriteable = true, cancelCallback = ::hide, keyEventHandler = ::keyEventHandler)

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            hide()
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

    private var state: ReplaceState = GET_SEARCH_ARG
        set(state) {
            field = state
            ui.title = getReplaceTitle()
        }

    private var isInhibitCancel = false

    override val isCancelInhibited: Boolean
        get() = isInhibitCancel

    private var isReplaced = false

    init {
        EditorUtil.disposeWithEditor(editor, this)

        lastSearch?.let { (search, replace) ->
            searchArg = search
            replaceArg = replace
            ui.text = getReplaceChoiceText()
            ui.selectText()
        }

        ui.title = getReplaceTitle()

        editor.caretModel.removeSecondaryCarets()
        editor.caretModel.addCaretListener(caretListener, this)

        identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)
        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, ERASE_MARKER)
    }

    internal fun show() {
        ui.show()
    }

    override fun release() {
        if (!editor.isDisposed) {
            editor.markupModel.removeAllHighlighters()

            editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)
        }
    }

    override fun clearDelegate() {
        ReplaceHandler.delegate = null
    }

    internal fun addNewLine() {
        if (state == GET_SEARCH_ARG || state == GET_REPLACE_ARG) {
            text += "\n"
        }
    }

    internal fun setTextFromPrevious(previous: Replace) {
        if (state == GET_SEARCH_ARG) {
            text = previous.search
        } else if (state == GET_REPLACE_ARG) {
            text = previous.replace
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun keyEventHandler(e: KeyEvent) {
        when (state) {
            GET_SEARCH_ARG -> {
                if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED && e.modifiersEx == 0) {
                    editor.markupModel.removeAllHighlighters()
                    val matchResult = Regex("(^.*) -> (.*)$", RegexOption.DOT_MATCHES_ALL).matchEntire(ui.text)?.destructured
                    if (matchResult != null) {
                        searchArg = matchResult.component1()
                        replaceArg = matchResult.component2()
                        startSearch()
                    } else {
                        setReplaceState()
                    }
                } else if (e.id == KeyEvent.KEY_RELEASED) {
                    editor.markupModel.removeAllHighlighters()
                    CommonHighlighter.findAllAndHighlight(
                        editor = editor,
                        project = project,
                        searchArg = ui.text,
                        useRegexp = type == REGEXP,
                        useCase = type == REGEXP || caseSensitive(ui.text),
                        range = selection
                    )
                }
            }
            GET_REPLACE_ARG -> {
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
                            ActionUtil.performAction(
                                recenterAction,
                                AnActionEvent.createEvent(
                                    DataManager.getInstance().getDataContext(editor.component),
                                    null,
                                    "Recenter",
                                    ActionUiKind.NONE,
                                    e
                                )
                            )
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
                            isInhibitCancel = true
                            ui.makeWriteable(replaceArg)
                            isInhibitCancel = false
                        }
                        '.' -> {
                            try {
                                replaceInEditor()
                            } catch (e: FindManager.MalformedReplacementStringException) {
                                handleReplacementError(e)
                            }
                            hide()
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
                            hide()
                        }
                    }
                }
            }
            ReplaceState.REPLACE_DONE, ReplaceState.REPLACE_FAILED -> {
                if (e.id == KeyEvent.KEY_PRESSED) {
                    hide()
                }
            }
        }
    }

    internal fun setReplaceState() {
        searchArg = ui.text
        state = GET_REPLACE_ARG
        ui.text = ""
        ReplaceHandler.resetPos()
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

    /**
     * Rewrites Emacs-style back references into the form [FindModel] expects: `\1`..`\9` become `$1`..`$9` and `\&`
     * becomes `$0`.
     *
     * Escapes are consumed left to right as whole tokens, which is what keeps `\\1` (a literal backslash followed by
     * a digit) apart from `\\\1` (a literal backslash followed by a back reference).
     */
    private fun fixBackReferences(replaceArgument: String): String =
        buildString {
            var index = 0
            while (index < replaceArgument.length) {
                val current = replaceArgument[index]
                val next = replaceArgument.getOrNull(index + 1)
                when {
                    current != '\\' || next == null -> {
                        append(current)
                        index++
                    }
                    next == '\\' -> {
                        append("""\\""")
                        index += 2
                    }
                    next.isDigit() -> {
                        append('$').append(next)
                        index += 2
                    }
                    next == '&' -> {
                        append("\$0")
                        index += 2
                    }
                    else -> {
                        append(current)
                        index++
                    }
                }
            }
        }

    private fun handleReplacementError(e: FindManager.MalformedReplacementStringException) {
        thisLogger().warn(e.message)
        ui.textColor = JBColor.RED
        editor.markupModel.removeAllHighlighters()
        state = ReplaceState.REPLACE_FAILED
    }

    private fun getReplaceTitle() =
        when (state) {
            GET_SEARCH_ARG -> if (type == REGEXP) "Query replace regexp: " else "Query replace: "
            GET_REPLACE_ARG, ReplaceState.SEARCHING, ReplaceState.EDIT_REPLACE_ARG -> "Replace $searchArg with: "
            ReplaceState.SEARCH_FOUND -> "Replace? "
            ReplaceState.REPLACE_DONE -> if (replaced == 1) "Replaced 1 occurrence." else "Replaced $replaced occurrences."
            ReplaceState.REPLACE_FAILED -> "Replacement failed. "
        }

    private fun getReplaceChoiceText(): String = "$searchArg -> $replaceArg"

    private fun searchForReplacement(highlight: Boolean) {
        val result = if (selection != null) {
            FindManager.getInstance(project).findString(editor.text.substring(0, selection.last), selection.first, replaceModel)
        } else {
            FindManager.getInstance(project).findString(editor.text, editor.caretModel.offset, replaceModel)
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
            val replacement = FindManager.getInstance(project)
                .getStringToReplace(foundString, replaceModel, lastResult.startOffset, editor.text)

            WriteCommandAction.runWriteCommandAction(project, "Replace ${type.name.lowercase()}", undoGroupId, {
                editor.document.replaceString(lastResult.startOffset, lastResult.endOffset, replacement)
            })

            replacements.addLast(Replaced(lastResult.startOffset, foundString, replacement))

            editor.caretModel.moveToOffset(lastResult.startOffset + replacement.length)

            replaced++
        }
    }

    private fun undoReplacement(item: Replaced) {
        WriteCommandAction.runWriteCommandAction(project, "Undo replace ${type.name.lowercase()}", undoGroupId, {
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
            project = project,
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
