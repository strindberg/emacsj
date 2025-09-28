package com.github.strindberg.emacsj.search

import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import java.util.regex.Pattern
import com.github.strindberg.emacsj.actions.search.ISearchAction
import com.github.strindberg.emacsj.paste.ACTION_PASTE
import com.github.strindberg.emacsj.preferences.EmacsJSettings
import com.github.strindberg.emacsj.search.Direction.BACKWARD
import com.github.strindberg.emacsj.search.Direction.FORWARD
import com.github.strindberg.emacsj.search.ISearchState.EDIT
import com.github.strindberg.emacsj.search.ISearchState.FAILED
import com.github.strindberg.emacsj.search.ISearchState.SEARCH
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.github.strindberg.emacsj.search.StartType.FIRST_SEARCH
import com.github.strindberg.emacsj.search.StartType.REPEATED_SEARCH
import com.github.strindberg.emacsj.search.StartType.WRAPAROUND
import com.github.strindberg.emacsj.view.ACTION_RECENTER
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
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

private const val ACTION_EDITOR_SCROLL_TO_CENTER = "EditorScrollToCenter"

private enum class StartType { WRAPAROUND, FIRST_SEARCH, REPEATED_SEARCH }

internal class ISearchDelegate(private val editor: Editor, val type: SearchType, var direction: Direction) {

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            cancel()
        }
    }

    private val identifierAttributes: TextAttributes

    private lateinit var typedHandler: RestorableTypedActionHandler

    private lateinit var actionHandlers: List<RestorableActionHandler<ISearchDelegate>>

    @VisibleForTesting
    internal val ui = CommonUI(editor, false, ::hide, ::keyEventHandler)

    @VisibleForTesting
    internal var state: ISearchState = SEARCH

    private val breadcrumbs = mutableListOf<EditorBreadcrumb>()

    private val rangeHighlighters = mutableListOf<RangeHighlighter>()

    private var inhibitCancel = false

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    init {
        editor.document.setReadOnly(true) // Prevent dead keys such as '^' and '~' from showing up in the editor while searching.

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

        initTitleText()

        setupTypedActionHandler()

        setupActionHandlers()

        ui.show()
    }

    internal fun initTitleText() {
        ui.title = titleText()
    }

    internal fun isActive() = state in listOf(SEARCH, FAILED)

    internal fun hide() {
        if (!inhibitCancel) {
            editor.document.setReadOnly(false)

            unregisterHandlers()

            editor.markupModel.removeAllHighlighters()

            editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

            editor.caretModel.removeCaretListener(caretListener)

            ISearchHandler.searchConcluded(text, type)

            ui.cancelUI()

            ISearchHandler.delegate = null

            editor.caretModel.runForEachCaret {
                it.clearData()
            }
        }
    }

    internal fun startEditedSearch() {
        setupTypedActionHandler()
        setupActionHandlers()

        state = SEARCH
        ui.makeReadonly(text, false)
        searchAllCarets(searchDirection = direction, newText = text.also { text = "" })
    }

    internal fun findFirst() {
        findFirstLast(FORWARD, direction == BACKWARD)
    }

    internal fun findLast() {
        findFirstLast(BACKWARD, direction == FORWARD)
    }

    internal fun editPrevious(previous: String) {
        text = previous
        if (state != EDIT) {
            edit()
        }
    }

    internal fun edit() {
        state = EDIT
        unregisterHandlers()
        inhibitCancel = true
        ui.makeWriteable(text)
        inhibitCancel = false
    }

    internal fun renewLaxState() {
        state = SEARCH
        updateUI(title = titleText(), text = text, found = true)

        ui.flashText(if (ISearchHandler.lax) "[match spaces loosely]" else "[match spaces literally]")

        removeAllHighlighters()

        val (isRegexp, searchString) = getSearchModelArguments()
        CommonHighlighter.findAllAndHighlight(editor, searchString, isRegexp, caseSensitive())
    }

    internal fun searchAllCarets(
        searchDirection: Direction,
        newText: String,
        keepStart: Boolean = true,
        forceWraparound: Boolean = false,
        saveBreadcrumb: Boolean = true,
    ) {
        if (saveBreadcrumb) {
            pushBreadcrumb()
        }

        val isNewText = newText.isNotEmpty()
        val startType = startType(isNewText || searchDirection != direction, forceWraparound)

        direction = searchDirection
        text += newText

        if (startType == WRAPAROUND) {
            editor.caretModel.removeSecondaryCarets()
        }

        removeHighlighters(isNewText)

        val result = editor.caretModel.allCarets.apply { if (direction == FORWARD) reverse() }.map { caret ->
            searchAndUpdate(caret, keepStart, startType)
        }.let { results ->
            if (editor.caretModel.caretCount == 1) results[0] else SearchResult(results.any { it.found }, null, false)
        }

        state = if (result.found) SEARCH else FAILED

        findAllAndHighlight(result.offset, isNewText)
        updateUI(result)
    }

    internal fun swapSearchStopAndThenCancel() {
        editor.caretModel.allCarets.forEach { caret ->
            if (caret.isValid) {
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.start else caret.search.match.end)
            }
        }
        cancel()
    }

    internal fun markSearchStopAndThenCancel() {
        (editor as? EditorEx)?.let {
            editor.caretModel.currentCaret.let { caret ->
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.start else caret.search.match.end)
                editor.startStickySelection()
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.end else caret.search.match.start)
            }
        }
        cancel()
    }

    private fun setupTypedActionHandler() {
        TypedAction.getInstance().apply {
            setupRawHandler(
                object : RestorableTypedActionHandler(rawHandler) {
                    override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
                        val delegate = ISearchHandler.delegate
                        if (delegate != null) {
                            when (delegate.state) {
                                EDIT -> delegate.text += charTyped.toString()
                                SEARCH, FAILED -> delegate.searchAllCarets(
                                    searchDirection = delegate.direction,
                                    newText = charTyped.toString()
                                )
                            }
                        } else {
                            myOriginalHandler?.execute(editor, charTyped, dataContext)
                        }
                    }
                }.also { typedHandler = it }
            )
        }
    }

    private fun setupActionHandlers() {
        EditorActionManager.getInstance().apply {
            actionHandlers = buildList {
                setActionHandler(
                    ACTION_EDITOR_BACKSPACE,
                    RestorableActionHandler(
                        ACTION_EDITOR_BACKSPACE,
                        getActionHandler(ACTION_EDITOR_BACKSPACE),
                        { ISearchHandler.delegate }
                    ) { _, _ ->
                        when (state) {
                            EDIT -> text = text.dropLast(1)
                            SEARCH, FAILED -> popBreadcrumb()
                        }
                    }.also { add(it) }
                )

                setActionHandler(
                    ACTION_EDITOR_ENTER,
                    RestorableActionHandler(
                        ACTION_EDITOR_ENTER,
                        getActionHandler(ACTION_EDITOR_ENTER),
                        { ISearchHandler.delegate }
                    ) { _, _ ->
                        when (state) {
                            EDIT -> startEditedSearch()
                            SEARCH, FAILED -> cancel()
                        }
                    }.also { add(it) }
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
                                EDIT -> text += ClipboardUtil.getTextInClipboard()
                                SEARCH, FAILED -> searchAllCarets(
                                    searchDirection = direction,
                                    newText = ClipboardUtil.getTextInClipboard() ?: ""
                                )
                            }
                        }.also { add(it) }
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
                            }.also { add(it) }
                        )
                    }
                }
            }
        }
    }

    private fun editorActions(): List<String> {
        val actionManager = ActionManager.getInstance()
        return actionManager.getActionIdList("").filter { actionId ->
            !actionManager.isGroup(actionId) &&
                actionManager.getAction(actionId)?.let { it is EditorAction && it !is ISearchAction } == true &&
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

    private fun keyEventHandler(e: KeyEvent) {
        // ESC or ctrl-g pressed
        if (e.id == KeyEvent.KEY_PRESSED &&
            (e.keyCode == VK_ESCAPE || (e.keyCode == VK_G && (e.modifiersEx and CTRL_DOWN_MASK == CTRL_DOWN_MASK)))
        ) {
            editor.caretModel.runForEachCaret { caret -> if (caret.isValid) caret.moveToOffset(caret.search.origin) }
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
        if (state == EDIT) {
            if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED && e.modifiersEx == 0) {
                editor.markupModel.removeAllHighlighters()
                startEditedSearch()
            } else if (e.id == KeyEvent.KEY_RELEASED) {
                editor.markupModel.removeAllHighlighters()
                CommonHighlighter.findAllAndHighlight(
                    editor = editor,
                    searchArg = ui.text,
                    useRegexp = type == REGEXP,
                    useCase = type == REGEXP || caseSensitive(ui.text),
                    range = null
                )
            }
        }
    }

    private fun cancel() {
        ui.cancelUI()
    }

    private fun unregisterHandlers() {
        TypedAction.getInstance().apply {
            setupRawHandler(typedHandler.originalHandler)
        }
        EditorActionManager.getInstance().apply {
            actionHandlers.forEach {
                setActionHandler(it.actionId, it.originalHandler)
            }
        }
    }

    private fun updateUI(result: SearchResult) {
        updateUI(titleText(found = result.found, wrapped = result.wrapped), text, result.found)
    }

    private fun updateUI(title: String, text: String, found: Boolean) {
        ui.title = title
        ui.text = text
        ui.textColor = if (found) JBColor.foreground() else JBColor.RED
    }

    private fun findFirstLast(findDirection: Direction, switchDirection: Boolean) {
        removeAllHighlighters()

        searchAllCarets(searchDirection = findDirection, newText = "", forceWraparound = true)

        if (switchDirection) {
            searchAllCarets(searchDirection = findDirection.reverse, newText = "", saveBreadcrumb = false)
        }
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
            val (isRegexp, searchString) = getSearchModelArguments()
            CommonHighlighter.findAllAndHighlight(editor, searchString, isRegexp, caseSensitive())
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

    private fun removeAllHighlighters() {
        CommonHighlighter.cancel(editor)
        rangeHighlighters.forEach {
            editor.markupModel.removeHighlighter(it)
        }
    }

    private fun searchAndUpdate(
        caret: Caret,
        keepStart: Boolean,
        startType: StartType,
    ): SearchResult {
        val result = findString(searchStart(caret.search, keepStart, startType))

        if (result.isStringFound) {
            moveAndUpdate(caret, Match(result.startOffset, result.endOffset), direction, true)
        }

        return SearchResult(result.isStringFound, if (result.isStringFound) result.startOffset else null, startType == WRAPAROUND)
    }

    private fun startType(firstSearch: Boolean, forceWraparound: Boolean): StartType =
        if (forceWraparound) {
            WRAPAROUND
        } else if (firstSearch) {
            FIRST_SEARCH
        } else if (state == FAILED) {
            WRAPAROUND
        } else {
            REPEATED_SEARCH
        }

    private fun searchStart(search: CaretSearch, keepStart: Boolean, startType: StartType): Int =
        when (direction) {
            FORWARD ->
                minOf(
                    when (startType) {
                        WRAPAROUND -> 0
                        FIRST_SEARCH -> search.match.start
                        REPEATED_SEARCH -> search.match.start + 1
                    },
                    editor.text.length
                )
            BACKWARD ->
                minOf(
                    when (startType) {
                        WRAPAROUND -> editor.text.length + 1
                        FIRST_SEARCH ->
                            // Mimic Emacs' behavior here:
                            // - When starting reverse search, always search from where the caret is.
                            // - When adding letters after a previous search, move search start rightward to include the new letters.
                            if (keepStart && search.origin == search.match.end) {
                                search.origin + 1
                            } else {
                                matchEnd(search.match.start) + 1
                            }
                        REPEATED_SEARCH -> search.match.end
                    },
                    editor.text.length + 1
                )
        }

    private fun matchEnd(start: Int): Int =
        start + if (type == TEXT) text.length else Regex(text).matchAt(editor.text, start)?.value?.length ?: 0

    private fun findAllAndHighlight(offset: Int?, highlight: Boolean) {
        val (isRegexp, searchString) = getSearchModelArguments()
        CommonHighlighter.findAllAndHighlight(
            editor,
            searchString,
            isRegexp,
            caseSensitive(),
            callback = { matches ->
                updateCount(Pair(matches.withIndex().find { it.value.startOffset == offset }?.let { it.index + 1 }, matches.size))
            },
            highlight = highlight
        )
    }

    private fun findString(offset: Int): FindResult {
        val (isRegexp, searchString) = getSearchModelArguments()
        return FindManager.getInstance(editor.project).findString(
            editor.text,
            offset,
            FindModel().apply {
                stringToFind = searchString
                isForward = direction == FORWARD
                isCaseSensitive = caseSensitive()
                isRegularExpressions = isRegexp
            }
        )
    }

    private fun getSearchModelArguments(): Pair<Boolean, String> =
        if (type == TEXT && ISearchHandler.lax) {
            Pair(
                true,
                text.split(Regex(" +")).filter { it.isNotBlank() }
                    .joinToString(EmacsJSettings.getInstance().state.searchWhitespaceRegexp) {
                        Pattern.quote(it)
                    }
            )
        } else {
            Pair(type == REGEXP, text)
        }

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
