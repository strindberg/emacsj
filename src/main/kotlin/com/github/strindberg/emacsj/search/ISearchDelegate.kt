package com.github.strindberg.emacsj.search

import java.awt.Color
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import java.lang.invoke.MethodHandles
import com.github.strindberg.emacsj.actions.paste.ACTION_PASTE
import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.search.Direction.BACKWARD
import com.github.strindberg.emacsj.search.Direction.FORWARD
import com.github.strindberg.emacsj.search.ISearchState.CHOOSE_PREVIOUS
import com.github.strindberg.emacsj.search.ISearchState.FAILED
import com.github.strindberg.emacsj.search.ISearchState.SEARCH
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_BACKSPACE
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_ENTER
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE
import com.intellij.openapi.application.ex.ClipboardUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.colors.EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import org.jetbrains.annotations.VisibleForTesting

private const val ACTION_EDITOR_SCROLL_TO_CENTER = "EditorScrollToCenter"
private const val ACTION_RECENTER = "com.github.strindberg.emacsj.actions.view.recenter"

internal class ISearchDelegate(val editor: Editor, val type: SearchType, var direction: Direction) {

    @Suppress("unused")
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            cancel()
        }
    }

    private val breadcrumbs = mutableListOf<EditorBreadcrumb>()

    private val identifierAttributes: TextAttributes

    private lateinit var typedHandler: ISearchTypedActionHandler

    private val actionHandlers = mutableListOf<ISearchActionHandler>()

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::keyEventHandler, ::hide)

    internal var state: ISearchState = SEARCH

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    init {
        editor.document.setReadOnly(true) // Prevent dead keys such as '^' and '~' from showing up in editor while searching.

        editor.caretModel.runForEachCaret {
            it.search = CaretSearch(it.offset)
            it.breadcrumbs = mutableListOf()
        }

        registerHandlers()

        ui.title = titleText()

        if (editor.selectionModel.hasSelection()) {
            editor.caretModel.removeSecondaryCarets()
        }
        editor.caretModel.addCaretListener(caretListener)

        identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)
        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, ERASE_MARKER)

        ui.show()
    }

    private fun registerHandlers() {
        TypedAction.getInstance().apply {
            setupRawHandler(
                object : ISearchTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        val delegate = ISearchHandler.delegate
                        if (delegate == null) {
                            myOriginalHandler?.execute(editor, charTyped, dataContext)
                        } else {
                            when (delegate.state) {
                                CHOOSE_PREVIOUS -> delegate.text += charTyped.toString()
                                SEARCH, FAILED -> delegate.searchAllCarets(delegate.direction, charTyped.toString())
                            }
                        }
                    }
                }.also { typedHandler = it }
            )
        }

        EditorActionManager.getInstance().apply {
            setActionHandler(
                ACTION_EDITOR_BACKSPACE,
                ISearchActionHandler(ACTION_EDITOR_BACKSPACE, getActionHandler(ACTION_EDITOR_BACKSPACE)) { _, _ ->
                    when (state) {
                        CHOOSE_PREVIOUS -> text = text.dropLast(1)
                        SEARCH, FAILED -> popBreadcrumb()
                    }
                }.also { actionHandlers.add(it) }
            )

            setActionHandler(
                ACTION_EDITOR_ENTER,
                ISearchActionHandler(ACTION_EDITOR_ENTER, getActionHandler(ACTION_EDITOR_ENTER)) { _, _ ->
                    when (state) {
                        CHOOSE_PREVIOUS -> startPreviousSearch()
                        SEARCH, FAILED -> cancel()
                    }
                }.also { actionHandlers.add(it) }
            )

            listOf(ACTION_EDITOR_PASTE, ACTION_PASTE).forEach { actionId ->
                setActionHandler(
                    actionId,
                    ISearchActionHandler(actionId, getActionHandler(actionId)) { _, _ ->
                        when (state) {
                            CHOOSE_PREVIOUS -> text += ClipboardUtil.getTextInClipboard()
                            SEARCH, FAILED -> searchAllCarets(direction, ClipboardUtil.getTextInClipboard() ?: "")
                        }
                    }.also { actionHandlers.add(it) }
                )
            }

            editorActions().forEach { actionId ->
                getActionHandler(actionId)?.let { originalHandler ->
                    setActionHandler(
                        actionId,
                        ISearchActionHandler(actionId, originalHandler) { caret, dataContext ->
                            cancel()
                            originalHandler.execute(editor, caret, dataContext)
                        }.also { actionHandlers.add(it) }
                    )
                }
            }
        }
    }

    private fun editorActions(): List<String> {
        val actionManager = ActionManager.getInstance()
        return actionManager.getActionIdList("").filter { actionId ->
            !actionManager.isGroup(actionId) &&
                actionManager.getAction(actionId)?.let { it is EditorAction && it !is ISearchAction } ?: false &&
                actionId !in listOf(
                    ACTION_EDITOR_BACKSPACE,
                    ACTION_EDITOR_ENTER,
                    ACTION_EDITOR_PASTE,
                    ACTION_PASTE,
                    ACTION_EDITOR_SCROLL_TO_CENTER,
                    ACTION_RECENTER
                )
        }
    }

    private fun keyEventHandler(e: KeyEvent): Boolean {
        if (e.id == KeyEvent.KEY_PRESSED && // ESC or ctrl-g
            (e.keyCode == VK_ESCAPE || (e.keyCode == VK_G && (e.modifiersEx and CTRL_DOWN_MASK == CTRL_DOWN_MASK)))
        ) {
            when (state) {
                CHOOSE_PREVIOUS -> ui.text = ""
                SEARCH, FAILED -> {
                    editor.caretModel.runForEachCaret { caret -> if (caret.isValid) caret.moveToOffset(caret.search.start) }
                    editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
                }
            }
        }
        return false
    }

    internal fun cancel() {
        ui.popup.cancel()
    }

    internal fun hide(): Boolean {
        unregisterHandlers()

        editor.markupModel.removeAllHighlighters()

        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

        editor.caretModel.removeCaretListener(caretListener)

        ISearchHandler.searchConcluded(text, type)

        editor.document.setReadOnly(false)

        ISearchHandler.delegate = null

        return true
    }

    private fun unregisterHandlers() {
        TypedAction.getInstance().apply {
            setupRawHandler(typedHandler.originalHandler)
        }
        EditorActionManager.getInstance().apply {
            actionHandlers.forEach {
                setActionHandler(it.action, it.originalHandler)
            }
        }
    }

    private fun updateUI(result: SearchResult, pos: Int?, noOfMatches: Int) {
        updateUI(titleText(result.found, result.wrapped), text, result.color, Pair(pos ?: 0, noOfMatches))
    }

    private fun updateUI(title: String, text: String, color: Color, count: Pair<Int, Int>?) {
        ui.title = title
        ui.text = text
        ui.textColor = color
        ui.count = count
    }

    private fun titleText(found: Boolean = true, wrapped: Boolean = false): String =
        listOfNotNull(
            if (!found) "Failing" else null,
            if (wrapped) "Wrapped" else null,
            if (type == REGEXP) "Regexp Search" else "Search",
            if (direction == BACKWARD) "Backward" else null
        ).joinToString(" ") + ": "

    internal fun startPreviousSearch() {
        state = SEARCH
        searchAllCarets(direction, text.also { text = "" })
    }

    private fun pushBreadcrumb() {
        if (editor.caretModel.allCarets.any { CaretBreadcrumb(it.search, direction) != it.breadcrumbs.lastOrNull() }) {
            editor.caretModel.runForEachCaret {
                it.breadcrumbs.add(CaretBreadcrumb(it.search, direction))
            }
            breadcrumbs.add(EditorBreadcrumb(ui.title, ui.text, ui.textColor, ui.count))
        }
    }

    private fun popBreadcrumb() {
        breadcrumbs.removeLastOrNull()?.let { breadcrumb ->
            updateUI(breadcrumb.title, breadcrumb.text, breadcrumb.color, breadcrumb.count)
            findAllAndHighlight(editor, text, type, caseSensitive())

            editor.caretModel.runForEachCaret { caret ->
                caret.breadcrumbs.removeLastOrNull()?.let { latest ->
                    moveAndUpdate(caret, latest.searchStart, latest.match, latest.direction)
                }
            }
        }
    }

    internal fun searchAllCarets(newDirection: Direction, newText: String = "", keepStart: Boolean = true) {
        pushBreadcrumb()

        val firstSearch = newText.isNotEmpty() || newDirection != direction
        val wraparound = state == FAILED && !firstSearch
        val single = editor.caretModel.caretCount == 1

        direction = newDirection
        text += newText

        if (text.isNotEmpty() && (single || state == SEARCH)) { // Don't wrap around on multi-caret search
            val matches = findAllAndHighlight(editor, text, type, caseSensitive())

            val results = editor.caretModel.allCarets.apply { if (direction == FORWARD) reverse() }.map { caret ->
                searchAndUpdate(caret, keepStart, firstSearch, wraparound)
            }

            val result = if (single) results[0] else SearchResult(results.any { it.found }, null, false)
            val pos = matches.withIndex().find { it.value.startOffset == result.offset }?.let { it.index + 1 }
            state = if (result.found) SEARCH else FAILED
            updateUI(result, pos, matches.size)
        }
    }

    private fun searchAndUpdate(caret: Caret, keepStart: Boolean, firstSearch: Boolean, wraparound: Boolean): SearchResult {
        val searchStart =
            with(caret.search) {
                if (firstSearch) {
                    if (direction == FORWARD) match.start else if (keepStart) searchStart else matchEnd(match.start)
                } else if (wraparound) {
                    if (direction == FORWARD) 0 else editor.document.textLength + 1
                } else {
                    null
                }
            }

        val result = findString(caret, searchStart)

        if (result.isStringFound) {
            moveAndUpdate(caret, searchStart, Match(result.startOffset, result.endOffset), direction)
        }

        return SearchResult(result.isStringFound, if (result.isStringFound) result.startOffset else null, wraparound)
    }

    private fun matchEnd(start: Int): Int =
        start + if (type == TEXT) text.length else Regex(text).matchAt(editor.text, start)?.value?.length ?: 0

    private fun findString(caret: Caret, searchStart: Int?): FindResult {
        return FindManager.getInstance(editor.project)
            .findString(
                editor.text,
                offset(editor.text, caret.search, searchStart),
                FindModel().apply {
                    stringToFind = text
                    isForward = direction == FORWARD
                    isCaseSensitive = caseSensitive()
                    isRegularExpressions = type == REGEXP
                }
            )
    }

    private fun offset(text: CharSequence, search: CaretSearch, searchStart: Int?): Int =
        when (direction) {
            FORWARD -> minOf(text.length, searchStart ?: (search.match.start + 1))
            BACKWARD -> minOf(text.length + 1, searchStart?.let { it + 1 } ?: search.match.end)
        }

    private fun caseSensitive() = type == REGEXP || caseSensitive(text)

    private fun moveAndUpdate(caret: Caret, searchStart: Int?, match: Match, direction: Direction) {
        if (caret.isValid) { // Caret might have been disposed after multi-caret search
            caret.moveToOffset(if (direction == FORWARD) match.end else match.start)
            caret.search = caret.search.copy(searchStart = searchStart ?: caret.search.searchStart, match = match)
            addHighlight(match)

            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            IdeDocumentHistory.getInstance(editor.project).includeCurrentCommandAsNavigation()
        }
    }

    private fun addHighlight(match: Match) {
        if (match.start != match.end) {
            editor.markupModel.addRangeHighlighter(
                EMACSJ_PRIMARY,
                match.start,
                match.end,
                HighlighterLayer.LAST + 2,
                HighlighterTargetArea.EXACT_RANGE
            )
        }
    }
}
