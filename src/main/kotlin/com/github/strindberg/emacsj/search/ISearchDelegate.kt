package com.github.strindberg.emacsj.search

import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import com.github.strindberg.emacsj.preferences.EmacsJSettings
import com.github.strindberg.emacsj.search.CaseType.INSENSITIVE
import com.github.strindberg.emacsj.search.CaseType.SENSITIVE
import com.github.strindberg.emacsj.search.CaseType.UNSPECIFIED
import com.github.strindberg.emacsj.search.ISearchState.EDIT
import com.github.strindberg.emacsj.search.ISearchState.FAILED
import com.github.strindberg.emacsj.search.ISearchState.SEARCH
import com.github.strindberg.emacsj.search.SearchDirection.BACKWARD
import com.github.strindberg.emacsj.search.SearchDirection.FORWARD
import com.github.strindberg.emacsj.search.SearchType.REGEXP
import com.github.strindberg.emacsj.search.SearchType.TEXT
import com.github.strindberg.emacsj.search.StartType.FIRST_SEARCH
import com.github.strindberg.emacsj.search.StartType.REPEATED_SEARCH
import com.github.strindberg.emacsj.search.StartType.WRAPAROUND
import com.github.strindberg.emacsj.ui.CommonUI
import com.github.strindberg.emacsj.ui.UIDelegate
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE
import com.intellij.openapi.editor.colors.EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

private enum class StartType { WRAPAROUND, FIRST_SEARCH, REPEATED_SEARCH }

internal class ISearchDelegate(editor: Editor, val project: Project, var searchType: SearchType, var direction: SearchDirection) :
    UIDelegate(editor) {

    @VisibleForTesting
    override val ui = CommonUI(editor = editor, isWriteable = false, cancelCallback = ::hide, keyEventHandler = ::keyEventHandler)

    @VisibleForTesting
    internal var state: ISearchState = SEARCH

    @VisibleForTesting
    internal var caseType: CaseType = UNSPECIFIED

    private var isInhibitCancel = false

    override val isCancelInhibited: Boolean
        get() = isInhibitCancel

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

    private val identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)

    private val breadcrumbs = ISearchBreadcrumbSupport(editor)

    /** The longest search string that matched, so that a failing search can call out only what was added after it. */
    private var foundText: String = ""

    private val killRing = ISearchKillRingSupport()

    init {
        EditorUtil.disposeWithEditor(editor, this)

        // Dead keys: the accent must not reach the document while it waits for its character.
        captureComposedInput { input -> handleChar(input) }

        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, NO_ATTRIBUTES)

        editor.caretModel.addCaretListener(caretListener, this)

        initTitleText()

        if (editor.selectionModel.hasSelection()) {
            editor.caretModel.removeSecondaryCarets()
        }

        if (editor.selectionModel.hasSelection() && ISearchHandler.isSelectionISearch) {
            searchSelected()
        } else {
            editor.caretModel.runForEachCaret {
                it.search = CaretSearch(it.offset)
            }
        }

        ui.show()
    }

    override fun release() {
        if (!editor.isDisposed) {
            clearAllHighlights()

            editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

            editor.caretModel.runForEachCaret {
                it.clearData()
            }
        }

        ISearchHandler.searchConcluded(text, searchType)
    }

    override fun clearDelegate() {
        ISearchHandler.delegate = null
    }

    internal fun initTitleText() {
        ui.title = titleText()
    }

    internal fun isActive() = state == SEARCH || state == FAILED

    internal fun startEditedSearch() {
        state = SEARCH
        ui.makeReadonly(text, false)
        searchAllCarets(searchDirection = direction, newText = text.also { text = "" })
    }

    internal fun handleEnter() {
        when (state) {
            EDIT -> startEditedSearch()
            SEARCH, FAILED -> hide()
        }
    }

    /** Only ever reached while the search is running; the popup's editor handles backspace once text is edited. */
    internal fun handleBackspace() {
        popBreadcrumb()
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
        isInhibitCancel = true
        try {
            ui.makeWriteable(text)
        } finally {
            isInhibitCancel = false
        }
    }

    internal fun paste(clipboard: String) {
        killRing.start(clipboard)
        addToSearch(clipboard)
    }

    internal fun pasteNextInHistory() {
        killRing.next()?.let { (replacedLength, inserted) ->
            text = text.dropLast(replacedLength)
            addToSearch(inserted)
        }
    }

    internal fun deleteChar() {
        if (text.isNotEmpty()) {
            text = text.take(text.length - 1)
            searchAllCarets(direction, "", forceFirstSearch = true)
        }
    }

    internal fun renewLaxState() {
        renewState(if (ISearchHandler.isLax) "[match spaces loosely]" else "[match spaces literally]")
    }

    internal fun toggleCaseSensitive() {
        caseType = when (caseType) {
            UNSPECIFIED -> if (defaultSensitive()) INSENSITIVE else SENSITIVE
            SENSITIVE -> INSENSITIVE
            INSENSITIVE -> SENSITIVE
        }
        renewState(if (caseType == SENSITIVE) "[case sensitive]" else "[case insensitive]")
    }

    internal fun toggleRegexpSearch() {
        searchType = when (searchType) {
            REGEXP -> TEXT
            TEXT -> REGEXP
        }
        renewState(null)
    }

    /** Searches in [searchDirection] for the current search string with [newText] added to it. */
    internal fun search(searchDirection: SearchDirection, newText: String = "") {
        searchAllCarets(searchDirection = searchDirection, newText = newText)
    }

    /**
     * Extends the search string with [newText] lifted from the document at the current match. Text arriving that way is
     * already behind a *backward* search's start, so such a search resumes from the end of the current match rather
     * than from where the search began.
     */
    internal fun expandSearch(newText: String) {
        searchAllCarets(searchDirection = direction, newText = newText, keepStart = false)
    }

    internal fun swapSearchStopAndThenCancel() {
        editor.caretModel.allCarets.forEach { caret ->
            if (caret.isValid) {
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.start else caret.search.match.end)
            }
        }
        hide()
    }

    internal fun markSearchStopAndThenCancel() {
        if (editor is EditorEx) {
            editor.caretModel.currentCaret.let { caret ->
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.start else caret.search.match.end)
                editor.startStickySelection()
                caret.moveToOffset(if (direction == FORWARD) caret.search.match.end else caret.search.match.start)
            }
        }
        hide()
    }

    /** Only ever reached while the search is running; the popup's editor handles typing once text is edited. */
    internal fun handleChar(charTyped: String) {
        killRing.invalidate()
        searchAllCarets(searchDirection = direction, newText = charTyped)
    }

    private fun keyEventHandler(e: KeyEvent) {
        // ESC or ctrl-g pressed
        if (e.id == KeyEvent.KEY_PRESSED &&
            (e.keyCode == VK_ESCAPE || (e.keyCode == VK_G && (e.modifiersEx and CTRL_DOWN_MASK == CTRL_DOWN_MASK)))
        ) {
            text = "" // Make sure search is not saved as previous search
            editor.caretModel.runForEachCaret { caret -> if (caret.isValid) caret.moveToOffset(caret.search.origin) }
            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
        }
        if (state == EDIT && e.id == KeyEvent.KEY_RELEASED) {
            clearAllHighlights()
            if (e.keyCode == VK_ENTER && e.modifiersEx == 0) startEditedSearch() else refreshHighlights()
        }
    }

    /** Only ever reached while the search is running; the popup's editor handles pasting once text is edited. */
    private fun addToSearch(newText: String) {
        searchAllCarets(searchDirection = direction, newText = newText, forceFirstSearch = true)
    }

    private fun searchSelected() {
        val selectedText = editor.selectionModel.selectedText.orEmpty()
        val origin = if (direction == FORWARD) editor.selectionModel.selectionStart else editor.selectionModel.selectionEnd

        editor.caretModel.primaryCaret.search = CaretSearch(origin)

        editor.selectionModel.removeSelection()
        if (editor is EditorEx) {
            editor.isStickySelection = false
        }

        searchAllCarets(direction, selectedText)
    }

    private fun findFirstLast(findDirection: SearchDirection, switchDirection: Boolean) {
        clearAllHighlights()

        searchAllCarets(searchDirection = findDirection, newText = "", forceWraparound = true)

        if (switchDirection) {
            searchAllCarets(searchDirection = findDirection.reverse, newText = "", saveBreadcrumb = false)
        }
    }

    private fun renewState(message: String?) {
        state = SEARCH
        updateUI(title = titleText(), text = text, found = true)

        message?.let { ui.flashText(it) }

        clearAllHighlights()

        refreshHighlights()
    }

    private fun searchAllCarets(
        searchDirection: SearchDirection,
        newText: String,
        keepStart: Boolean = true,
        forceWraparound: Boolean = false,
        forceFirstSearch: Boolean = false,
        saveBreadcrumb: Boolean = true,
    ) {
        if (saveBreadcrumb) {
            pushBreadcrumb()
        }

        val isNewText = newText.isNotEmpty() || forceFirstSearch
        val startType = startType(isNewText || searchDirection != direction, forceWraparound)

        direction = searchDirection
        text += newText

        if (startType == WRAPAROUND) {
            editor.caretModel.removeSecondaryCarets()
        }

        if (isNewText) clearAllHighlights() else clearCurrentMatchHighlights()

        val (isRegexp, searchString) = getSearchModelArguments()

        val result = editor.caretModel.allCarets.apply { if (direction == FORWARD) reverse() }.map { caret ->
            searchAndUpdate(caret, keepStart, startType, isRegexp, searchString)
        }.let { results ->
            if (editor.caretModel.caretCount == 1) results[0] else SearchResult(results.any { it.isFound }, null, false)
        }

        state = if (result.isFound) SEARCH else FAILED

        refreshHighlightsAndCount(offset = result.offset, highlight = isNewText)

        updateUI(result)
    }

    private fun pushBreadcrumb() {
        breadcrumbs.push(
            EditorBreadcrumb(
                title = ui.title,
                text = ui.text,
                direction = direction,
                state = state,
                caseType = caseType,
                searchType = searchType,
                count = ui.count
            )
        )
    }

    private fun popBreadcrumb() {
        breadcrumbs.pop()?.let { breadcrumb ->
            val searchChanged =
                breadcrumb.text != ui.text || breadcrumb.caseType != caseType || breadcrumb.searchType != searchType
            if (searchChanged) clearAllHighlights() else clearCurrentMatchHighlights()

            state = breadcrumb.state
            caseType = breadcrumb.caseType
            searchType = breadcrumb.searchType
            updateUI(breadcrumb.title, breadcrumb.text, breadcrumb.state == SEARCH)
            updateCount(breadcrumb.count)

            editor.caretModel.runForEachCaret { caret ->
                breadcrumbs.popMatch(caret)?.let { latest ->
                    moveAndUpdate(caret = caret, match = latest, direction = breadcrumb.direction, found = breadcrumb.state == SEARCH)
                }
            }
            refreshHighlights()
        }
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

    private fun updateUI(result: SearchResult) {
        updateUI(titleText(found = result.isFound, wrapped = result.isWrapped), text, result.isFound)
    }

    private fun updateUI(title: String, text: String, found: Boolean) {
        ui.title = title
        ui.textColor = JBColor.foreground()

        if (found) {
            foundText = text
            ui.showText(text)
        } else {
            val matched = text.commonPrefixWith(foundText)
            ui.showText(matched, text.substring(matched.length))
        }
    }

    private fun titleText(found: Boolean = true, wrapped: Boolean = false): String =
        listOfNotNull(
            if (!found) "Failing" else null,
            if (wrapped) "Wrapped" else null,
            if (searchType == REGEXP) "Regexp Search" else "Search",
            if (direction == BACKWARD) "Backward" else null
        ).joinToString(" ") + ": "

    private fun updateCount(count: Pair<Int?, Int>?) {
        ui.count = count?.let { Pair(count.first ?: 0, count.second) }
    }

    /** Removes every highlight this session painted, and stops any search still on its way to painting more. */
    private fun clearAllHighlights() {
        CommonHighlighter.instance.cancelPending()
        editor.removeHighlights(EMACSJ_PRIMARY, EMACSJ_SECONDARY)
    }

    /** Removes the markers on the current match only. The search is unchanged, so the secondary highlights stay. */
    private fun clearCurrentMatchHighlights() {
        editor.removeHighlights(EMACSJ_PRIMARY)
    }

    private fun searchAndUpdate(
        caret: Caret,
        keepStart: Boolean,
        startType: StartType,
        isRegexp: Boolean,
        searchString: String,
    ): SearchResult {
        val result = findString(searchStart(caret.search, keepStart, startType), isRegexp, searchString)

        if (result.isStringFound) {
            moveAndUpdate(caret = caret, match = Match(result.startOffset, result.endOffset), direction = direction, found = true)
        }

        return SearchResult(result.isStringFound, if (result.isStringFound) result.startOffset else null, startType == WRAPAROUND)
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
        start + if (searchType == TEXT) {
            text.length
        } else {
            try {
                Regex(text).matchAt(editor.text, start)?.run {
                    value.length
                } ?: 0
            } catch (_: PatternSyntaxException) {
                // Half-typed patterns such as "(" or "[" are normal while a regexp is being composed.
                0
            }
        }

    private fun refreshHighlightsAndCount(offset: Int?, highlight: Boolean) {
        refreshHighlights(highlight) { matches ->
            updateCount(Pair(matches.withIndex().find { it.value.startOffset == offset }?.let { it.index + 1 }, matches.size))
        }
    }

    private fun refreshHighlights(highlight: Boolean = true, callback: (List<FindResult>) -> Unit = {}) {
        val (isRegexp, searchString) = getSearchModelArguments()
        CommonHighlighter.instance.findAllAndHighlight(
            SearchRequest(
                editor = editor,
                project = project,
                searchArg = searchString,
                useRegexp = isRegexp,
                useCase = caseSensitive(),
                callback = callback,
                highlight = highlight
            )
        )
    }

    // A malformed regexp needs no handling here: FindManager reports a not-found result rather than throwing.
    private fun findString(offset: Int, isRegexp: Boolean, searchString: String): FindResult =
        FindManager.getInstance(project)
            .findString(
                editor.text,
                offset,
                FindModel().apply {
                    stringToFind = searchString
                    isForward = direction == FORWARD
                    isCaseSensitive = caseSensitive()
                    isRegularExpressions = isRegexp
                }
            )

    private fun getSearchModelArguments(): Pair<Boolean, String> =
        if (searchType == TEXT && ISearchHandler.isLax) {
            Pair(
                true,
                text.split(Regex(" +")).filter { it.isNotBlank() }
                    .joinToString(EmacsJSettings.getInstance().state.searchWhitespaceRegexp) { Pattern.quote(it) }
            )
        } else {
            Pair(searchType == REGEXP, text)
        }

    private fun caseSensitive() =
        when (caseType) {
            SENSITIVE -> true
            INSENSITIVE -> false
            UNSPECIFIED -> defaultSensitive()
        }

    private fun defaultSensitive() = searchType == REGEXP || caseSensitive(text)

    private fun moveAndUpdate(caret: Caret, match: Match, direction: SearchDirection, found: Boolean) {
        if (caret.isValid) { // Caret might have been disposed after multi-caret search
            caret.moveToOffset(if (direction == FORWARD) match.end else match.start)
            caret.search = caret.search.copy(match = match)
            if (found) {
                addPrimaryHighlight(match)
            }

            editor.scrollingModel.scrollToCaret(MAKE_VISIBLE)
            IdeDocumentHistory.getInstance(project).includeCurrentCommandAsNavigation()
        }
    }

    private fun addPrimaryHighlight(match: Match) {
        editor.markupModel.addRangeHighlighter(
            EMACSJ_PRIMARY,
            match.start,
            match.end,
            HighlighterLayer.LAST + 2,
            HighlighterTargetArea.EXACT_RANGE
        )
    }
}
