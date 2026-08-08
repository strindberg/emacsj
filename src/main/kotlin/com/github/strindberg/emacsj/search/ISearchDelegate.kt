package com.github.strindberg.emacsj.search

import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.VK_ENTER
import java.awt.event.KeyEvent.VK_ESCAPE
import java.awt.event.KeyEvent.VK_G
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import com.github.strindberg.emacsj.paste.clipboardHistoryTexts
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
import com.github.strindberg.emacsj.ui.constructInput
import com.github.strindberg.emacsj.ui.isActive
import com.github.strindberg.emacsj.word.text
import com.intellij.find.FindManager
import com.intellij.find.FindModel
import com.intellij.find.FindResult
import com.intellij.ide.IdeEventQueue
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
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes.ERASE_MARKER
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import org.jetbrains.annotations.VisibleForTesting

private enum class StartType { WRAPAROUND, FIRST_SEARCH, REPEATED_SEARCH }

internal class ISearchDelegate(editor: Editor, val project: Project, var searchType: SearchType, var direction: SearchDirection) :
    UIDelegate(editor) {

    private val caretListener = object : CaretListener {
        override fun caretAdded(e: CaretEvent) {
            hide()
        }
    }

    private val identifierAttributes = editor.colorsScheme.getAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES)

    @VisibleForTesting
    override val ui = CommonUI(editor = editor, isWriteable = false, cancelCallback = ::hide, keyEventHandler = ::keyEventHandler)

    @VisibleForTesting
    internal var state: ISearchState = SEARCH

    private val breadcrumbs = mutableListOf<EditorBreadcrumb>()

    private val primaryHighlighters = mutableListOf<RangeHighlighter>()

    private var isInhibitCancel = false

    override val isCancelInhibited: Boolean
        get() = isInhibitCancel

    /** The longest search string that matched, so that a failing search can call out only what was added after it. */
    private var foundText: String = ""

    /** What the most recent paste added, so that walking the clipboard history can take it back out again. */
    private var pastedText: String = ""

    private var clipboardHistory: List<String> = emptyList()

    private var clipboardHistoryPos = 0

    @VisibleForTesting
    internal var caseType: CaseType = UNSPECIFIED

    internal var text: String
        get() = ui.text
        set(newText) {
            ui.text = newText
        }

    init {
        EditorUtil.disposeWithEditor(editor, this)

        // Handle dead keys, i.e. accents waiting for their main character. If this dispatcher is not used,
        // typed accents show in the editor until the full character is composed.
        IdeEventQueue.getInstance().addDispatcher(
            { e ->
                val delegate = ISearchHandler.delegate
                if (delegate.isActive(e)) {
                    e.constructInput()?.let { delegate.handleChar(it) }
                    e.consume()
                    true
                } else {
                    false
                }
            },
            this
        )

        editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, ERASE_MARKER)

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

    internal fun initTitleText() {
        ui.title = titleText()
    }

    internal fun isActive() = state == SEARCH || state == FAILED

    override fun release() {
        if (!editor.isDisposed) {
            editor.markupModel.removeAllHighlighters()

            editor.colorsScheme.setAttributes(IDENTIFIER_UNDER_CARET_ATTRIBUTES, identifierAttributes)

            editor.caretModel.runForEachCaret {
                it.clearData()
            }
        }

        ISearchHandler.searchConcluded(text, searchType)
    }

    internal fun handleEnter() {
        when (state) {
            EDIT -> startEditedSearch()
            SEARCH, FAILED -> hide()
        }
    }

    internal fun handleBackspace() {
        when (state) {
            EDIT -> text = text.dropLast(1)
            SEARCH, FAILED -> popBreadcrumb()
        }
    }

    override fun clearDelegate() {
        ISearchHandler.delegate = null
    }

    internal fun startEditedSearch() {
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
        isInhibitCancel = true
        ui.makeWriteable(text)
        isInhibitCancel = false
    }

    internal fun paste(clipboard: String) {
        clipboardHistory = clipboardHistoryTexts()
        clipboardHistoryPos = 0
        pastedText = clipboard

        addToSearch(clipboard)
    }

    internal fun pasteNextInHistory() {
        // Typing into the search string reaches neither listener -- the raw typed handler consumes it before it can
        // become an action or a command -- so the caller's "was the last action a paste" check cannot see it. The
        // paste is therefore invalidated here, by whatever else changed the search string.
        if (clipboardHistory.isEmpty() || pastedText.isEmpty()) {
            return
        }

        clipboardHistoryPos = (clipboardHistoryPos + 1) % clipboardHistory.size
        val next = clipboardHistory[clipboardHistoryPos]

        text = text.dropLast(pastedText.length)
        pastedText = next

        addToSearch(next)
    }

    internal fun deleteChar() {
        pastedText = ""
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

    internal fun searchAllCarets(
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

        removeHighlighters(isNewText)

        val (isRegexp, searchString) = getSearchModelArguments()

        val result = editor.caretModel.allCarets.apply { if (direction == FORWARD) reverse() }.map { caret ->
            searchAndUpdate(caret, keepStart, startType, isRegexp, searchString)
        }.let { results ->
            if (editor.caretModel.caretCount == 1) results[0] else SearchResult(results.any { it.isFound }, null, false)
        }

        state = if (result.isFound) SEARCH else FAILED

        findAllAndHighlight(offset = result.offset, highlight = isNewText, isRegexp = isRegexp, searchString = searchString)

        updateUI(result)
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

    internal fun handleChar(charTyped: String) {
        pastedText = ""
        when (state) {
            EDIT -> text += charTyped
            SEARCH, FAILED -> searchAllCarets(
                searchDirection = direction,
                newText = charTyped
            )
        }
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
        if (state == EDIT) {
            if (e.keyCode == VK_ENTER && e.id == KeyEvent.KEY_RELEASED && e.modifiersEx == 0) {
                editor.markupModel.removeAllHighlighters()
                startEditedSearch()
            } else if (e.id == KeyEvent.KEY_RELEASED) {
                editor.markupModel.removeAllHighlighters()
                CommonHighlighter.findAllAndHighlight(
                    editor = editor,
                    project = project,
                    searchArg = ui.text,
                    useRegexp = searchType == REGEXP,
                    useCase = searchType == REGEXP || caseSensitive(ui.text),
                    range = null
                )
            }
        }
    }

    private fun addToSearch(newText: String) {
        when (state) {
            EDIT -> text += newText
            SEARCH, FAILED -> searchAllCarets(searchDirection = direction, newText = newText, forceFirstSearch = true)
        }
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

    private fun renewState(message: String?) {
        state = SEARCH
        updateUI(title = titleText(), text = text, found = true)

        message?.let { ui.flashText(it) }

        removeAllHighlighters()

        val (isRegexp, searchString) = getSearchModelArguments()
        CommonHighlighter.findAllAndHighlight(
            editor = editor,
            project = project,
            searchArg = searchString,
            useRegexp = isRegexp,
            useCase = caseSensitive()
        )
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
        removeAllHighlighters()

        searchAllCarets(searchDirection = findDirection, newText = "", forceWraparound = true)

        if (switchDirection) {
            searchAllCarets(searchDirection = findDirection.reverse, newText = "", saveBreadcrumb = false)
        }
    }

    private fun updateCount(count: Pair<Int?, Int>?) {
        ui.count = count?.let { Pair(count.first ?: 0, count.second) }
    }

    private fun titleText(found: Boolean = true, wrapped: Boolean = false): String =
        listOfNotNull(
            if (!found) "Failing" else null,
            if (wrapped) "Wrapped" else null,
            if (searchType == REGEXP) "Regexp Search" else "Search",
            if (direction == BACKWARD) "Backward" else null
        ).joinToString(" ") + ": "

    private fun pushBreadcrumb() {
        if (breadcrumbs.lastOrNull()?.text != text || breadcrumbs.lastOrNull()?.direction != direction ||
            editor.caretModel.allCarets.any { it.search.match != it.breadcrumbs.lastOrNull() }
        ) {
            editor.caretModel.runForEachCaret {
                it.breadcrumbs.add(it.search.match)
            }
            breadcrumbs.add(
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
    }

    private fun popBreadcrumb() {
        breadcrumbs.removeLastOrNull()?.let { breadcrumb ->
            removeHighlighters(breadcrumb.text != ui.text || breadcrumb.caseType != caseType || breadcrumb.searchType != searchType)

            state = breadcrumb.state
            caseType = breadcrumb.caseType
            searchType = breadcrumb.searchType
            updateUI(breadcrumb.title, breadcrumb.text, breadcrumb.state == SEARCH)
            updateCount(breadcrumb.count)

            editor.caretModel.runForEachCaret { caret ->
                caret.breadcrumbs.removeLastOrNull()?.let { latest ->
                    moveAndUpdate(caret = caret, match = latest, direction = breadcrumb.direction, found = breadcrumb.state == SEARCH)
                }
            }
            val (isRegexp, searchString) = getSearchModelArguments()
            CommonHighlighter.findAllAndHighlight(
                editor = editor,
                project = project,
                searchArg = searchString,
                useRegexp = isRegexp,
                useCase = caseSensitive(),
            )
        }
    }

    private fun removeHighlighters(isNewText: Boolean) {
        if (isNewText) {
            CommonHighlighter.cancel(editor)
        } else {
            primaryHighlighters.forEach {
                editor.markupModel.removeHighlighter(it)
            }
        }
    }

    private fun removeAllHighlighters() {
        CommonHighlighter.cancel(editor)
        primaryHighlighters.forEach {
            editor.markupModel.removeHighlighter(it)
        }
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

    private fun findAllAndHighlight(offset: Int?, highlight: Boolean, isRegexp: Boolean, searchString: String) {
        CommonHighlighter.findAllAndHighlight(
            editor = editor,
            project = project,
            searchArg = searchString,
            useRegexp = isRegexp,
            useCase = caseSensitive(),
            callback = { matches ->
                updateCount(Pair(matches.withIndex().find { it.value.startOffset == offset }?.let { it.index + 1 }, matches.size))
            },
            highlight = highlight
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
        primaryHighlighters.add(
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
