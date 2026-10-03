package com.github.strindberg.emacsj.actions.selection

import com.github.strindberg.emacsj.selection.SelectionHandler
import com.github.strindberg.emacsj.selection.SelectionType
import com.intellij.openapi.editor.actionSystem.EditorAction

class SelectNextOccurrenceAction : EditorAction(SelectionHandler(SelectionType.NEXT))
