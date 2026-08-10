package com.github.strindberg.emacsj.search

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor

/**
 * A breadcrumb is two things that have to move together: one [EditorBreadcrumb] describing the session, and
 * one [Match] per caret. The per-caret half lives in caret user data rather than in a list here, so keeping both
 * behind this class is what stops them being pushed or popped independently.
 */
internal class ISearchBreadcrumbSupport(private val editor: Editor) {

    private val crumbs = mutableListOf<EditorBreadcrumb>()

    fun push(crumb: EditorBreadcrumb) {
        val last = crumbs.lastOrNull()
        if (last?.text != crumb.text || last.direction != crumb.direction ||
            editor.caretModel.allCarets.any { it.search.match != it.breadcrumbs.lastOrNull() }
        ) {
            editor.caretModel.runForEachCaret {
                it.breadcrumbs.add(it.search.match)
            }
            crumbs.add(crumb)
        }
    }

    fun pop(): EditorBreadcrumb? = crumbs.removeLastOrNull()

    /** The match [caret] held at the step [pop] just returned. */
    fun popMatch(caret: Caret): Match? = caret.breadcrumbs.removeLastOrNull()
}
