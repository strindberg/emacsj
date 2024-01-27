package com.github.strindberg.emacsj.search

import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
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
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

private const val ACTION_EDITOR_SCROLL_TO_CENTER = "EditorScrollToCenter"
private const val ACTION_RECENTER = "com.github.strindberg.emacsj.actions.view.recenter"

internal class ISearchDelegate(val editor: Editor, val type: SearchType, var direction: Direction) {

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            cancel()
        }
    }

    private val identifierAttributes: TextAttributes

    private val typedHandler: RestorableTypedActionHandler

    private val actionHandlers: List<RestorableActionHandler<ISearchDelegate>>

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::keyEventHandler, ::hide)

    internal var state: ISearchState = SEARCH

    private val breadcrumbs = mutableListOf<EditorBreadcrumb>()

    private val rangeHighlighters = mutableListOf<RangeHighlighter>()

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    init {
        editor.document.setReadOnly(true) // Prevent dead keys such as '^' and '~' from showing up in editor while searching.

        identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)
        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, ERASE_MARKER)

        if (editor.selectionModel.hasSelection()) {
            editor.caretModel.removeSecondaryCarets()
        }

        editor.caretModel.addCaretListener(caretListener)

        editor.caretModel.runForEachCaret {
            it.search = CaretSearch(it.offset)
            it.breadcrumbs = mutableListOf()
        }

        ui.title = titleText()

        TypedAction.getInstance().apply {
            setupRawHandler(
                object : RestorableTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        val delegate = ISearchHandler.delegate
                        if (delegate == null) {
                            myOriginalHandler?.execute(editor, charTyped, dataContext)
                        } else {
                            when (delegate.state) {
                                CHOOSE_PREVIOUS -> delegate.text += charTyped.toString()
                                SEARCH, FAILED -> delegate.searchAllCarets(delegate.direction, charTyped.toString(), keepStart = true)
                            }
                        }
                    }
                }.also { typedHandler = it }
            )
        }
    }

    init {
        EditorActionManager.getInstance().apply {
            val handlers = mutableListOf<RestorableActionHandler<ISearchDelegate>>()

            setActionHandler(
                ACTION_EDITOR_BACKSPACE,
                RestorableActionHandler(
                    ACTION_EDITOR_BACKSPACE,
                    getActionHandler(ACTION_EDITOR_BACKSPACE),
                    { ISearchHandler.delegate }
                ) { _, _ ->
                    when (state) {
                        CHOOSE_PREVIOUS -> text = text.dropLast(1)
                        SEARCH, FAILED -> popBreadcrumb()
                    }
                }.also { handlers.add(it) }
            )

            setActionHandler(
                ACTION_EDITOR_ENTER,
                RestorableActionHandler(
                    ACTION_EDITOR_ENTER,
                    getActionHandler(ACTION_EDITOR_ENTER),
                    { ISearchHandler.delegate }
                ) { _, _ ->
                    when (state) {
                        CHOOSE_PREVIOUS -> startPreviousSearch()
                        SEARCH, FAILED -> cancel()
                    }
                }.also { handlers.add(it) }
            )

            listOf(ACTION_EDITOR_PASTE, ACTION_PASTE).forEach { actionId ->
                setActionHandler(
                    actionId,
                    RestorableActionHandler(
                        actionId,
                        getActionHandler(actionId),
                        { ISearchHandler.delegate }
                    ) { _, _ ->
                        when (state) {
                            CHOOSE_PREVIOUS -> text += ClipboardUtil.getTextInClipboard()
                            SEARCH, FAILED -> searchAllCarets(direction, ClipboardUtil.getTextInClipboard() ?: "", keepStart = true)
                        }
                    }.also { handlers.add(it) }
                )
            }

            editorActions().forEach { actionId ->
                getActionHandler(actionId)?.let { originalHandler ->
                    setActionHandler(
                        actionId,
                        RestorableActionHandler(
                            actionId,
                            originalHandler,
                            { ISearchHandler.delegate }
                        ) { caret, dataContext ->
                            cancel()
                            originalHandler.execute(editor, caret, dataContext)
                        }.also { handlers.add(it) }
                    )
                }
            }

            actionHandlers = handlers
        }
    }

    init {
        ui.show()
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
                    editor.caretModel.runForEachCaret { caret -> if (caret.isValid) caret.moveToOffset(caret.search.origin) }
                    editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
                }
            }
        }
        return false
    }

    private fun cancel() {
        ui.popup.cancel()
    }

    internal fun hide(): Boolean {
        unregisterHandlers()

        editor.markupModel.removeAllHighlighters()

        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

        editor.caretModel.removeCaretListener(caretListener)

        editor.document.setReadOnly(false)

        ISearchHandler.searchConcluded(text, type)

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

    private fun updateUI(result: SearchResult) {
        updateUI(titleText(result.found, result.wrapped), text, result.found)
    }

    private fun updateUI(title: String, text: String, found: Boolean) {
        ui.title = title
        ui.text = text
        ui.textColor = if (found) JBColor.foreground() else JBColor.RED
    }

    private fun updateCount(count: Pair<Int?, Int>?) {
        ui.count = if (count != null) Pair(count.first ?: 0, count.second) else null
    }

    private fun titleText(found: Boolean = true, wrapped: Boolean = false): String =
        listOfNotNull(
            if (!found) "Failing" else null,
            if (wrapped) "Wrapped" else null,
            if (type == REGEXP) "Regexp Search" else "Search",
            if (direction == BACKWARD) "Backward" else null
        ).joinToString(" ") + ": "

    private fun pushBreadcrumb() {
        if (breadcrumbs.lastOrNull()?.text != ui.text ||
            editor.caretModel.allCarets.any { CaretBreadcrumb(it.search.match, direction) != it.breadcrumbs.lastOrNull() }
        ) {
            editor.caretModel.runForEachCaret {
                it.breadcrumbs.add(CaretBreadcrumb(it.search.match, direction))
            }
            breadcrumbs.add(EditorBreadcrumb(ui.title, ui.text, state, ui.count))
        }
    }

    private fun popBreadcrumb() {
        breadcrumbs.removeLastOrNull()?.let { breadcrumb ->
            removeHighlighters(breadcrumb.text != ui.text)

            state = breadcrumb.state
            updateUI(breadcrumb.title, breadcrumb.text, breadcrumb.state == SEARCH)
            updateCount(breadcrumb.count)

            editor.caretModel.runForEachCaret { caret ->
                caret.breadcrumbs.removeLastOrNull()?.let { latest ->
                    moveAndUpdate(caret, latest.match, latest.direction, breadcrumb.state == SEARCH)
                }
            }
            CommonHighlighter.findAllAndHighlight(editor, text, type, caseSensitive())
        }
    }

    internal fun startPreviousSearch() {
        state = SEARCH
        searchAllCarets(direction, text.also { text = "" }, keepStart = true)
    }

    internal fun searchAllCarets(newDirection: Direction, newText: String = "", keepStart: Boolean) {
        pushBreadcrumb()

        val isNewText = newText.isNotEmpty()
        val firstSearch = isNewText || newDirection != direction
        val wraparound = state == FAILED && !firstSearch
        val single = editor.caretModel.caretCount == 1

        direction = newDirection
        text += newText

        if (single || state == SEARCH) { // Don't wrap around on multi-caret search
            removeHighlighters(isNewText)
            val results = editor.caretModel.allCarets.apply { if (direction == FORWARD) reverse() }.map { caret ->
                searchAndUpdate(caret, keepStart, firstSearch, wraparound)
            }
            val result = if (single) results[0] else SearchResult(results.any { it.found }, null, false)
            state = if (result.found) SEARCH else FAILED
            findAllAndHighlight(result.offset, isNewText)
            updateUI(result)
        }
    }

    private fun removeHighlighters(isNewText: Boolean) {
        if (isNewText) {
            CommonHighlighter.cancel(editor)
        } else {
            rangeHighlighters.forEach {
                editor.markupModel.removeHighlighter(it)
            }
        }
    }

    private fun findAllAndHighlight(offset: Int?, highlight: Boolean) {
        CommonHighlighter.findAllAndHighlight(
            editor,
            text,
            type,
            caseSensitive(),
            callback = { matches ->
                updateCount(Pair(matches.withIndex().find { it.value.startOffset == offset }?.let { it.index + 1 }, matches.size))
            },
            highlight = highlight
        )
    }

    private fun searchAndUpdate(caret: Caret, keepStart: Boolean, firstSearch: Boolean, wraparound: Boolean): SearchResult {
        val result = findString(searchStart(caret.search, keepStart, firstSearch, wraparound))

        if (result.isStringFound) {
            moveAndUpdate(caret, Match(result.startOffset, result.endOffset), direction, true)
        }

        return SearchResult(result.isStringFound, if (result.isStringFound) result.startOffset else null, wraparound)
    }

    private fun searchStart(search: CaretSearch, keepStart: Boolean, firstSearch: Boolean, wraparound: Boolean) =
        when (direction) {
            FORWARD ->
                minOf(
                    if (firstSearch) {
                        search.match.start
                    } else if (wraparound) {
                        0
                    } else {
                        search.match.start + 1
                    },
                    editor.text.length
                )
            BACKWARD ->
                minOf(
                    if (firstSearch) {
                        // Mimic Emacs' behavior here:
                        // - When starting reverse search, always search from where the caret is.
                        // - When adding letters after previous search, move search start rightward to include the new letters.
                        if (keepStart && search.origin == search.match.end) {
                            search.origin + 1
                        } else {
                            matchEnd(search.match.start) + 1
                        }
                    } else if (wraparound) {
                        editor.text.length + 1
                    } else {
                        search.match.end
                    },
                    editor.text.length + 1
                )
        }

    private fun matchEnd(start: Int): Int =
        start + if (type == TEXT) text.length else Regex(text).matchAt(editor.text, start)?.value?.length ?: 0

    private fun findString(offset: Int): FindResult =
        FindManager.getInstance(editor.project).findString(
            editor.text,
            offset,
            FindModel().apply {
                stringToFind = text
                isForward = direction == FORWARD
                isCaseSensitive = caseSensitive()
                isRegularExpressions = type == REGEXP
            }
        )

    private fun caseSensitive() = type == REGEXP || caseSensitive(text)

    private fun moveAndUpdate(caret: Caret, match: Match, direction: Direction, found: Boolean) {
        if (caret.isValid) { // Caret might have been disposed after multi-caret search
            caret.moveToOffset(if (direction == FORWARD) match.end else match.start)
            caret.search = caret.search.copy(match = match)
            if (found) {
                addHighlight(match)
            }

            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            IdeDocumentHistory.getInstance(editor.project).includeCurrentCommandAsNavigation()
        }
    }

    private fun addHighlight(match: Match) {
        rangeHighlighters.add(
            editor.markupModel.addRangeHighlighter(
                EMACSJ_PRIMARY,
                match.start,
                match.end,
                HighlighterLayer.LAST + 2,
                HighlighterTargetArea.EXACT_RANGE
            )
        )
    }
}
