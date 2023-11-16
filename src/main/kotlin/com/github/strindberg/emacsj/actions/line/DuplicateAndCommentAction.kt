package com.github.strindberg.emacsj.actions.line

import com.github.strindberg.emacsj.line.DuplicateAndCommentHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class DuplicateAndCommentAction : TextComponentEditorAction(DuplicateAndCommentHandler())
