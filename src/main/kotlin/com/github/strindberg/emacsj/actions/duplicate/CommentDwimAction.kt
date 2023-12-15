package com.github.strindberg.emacsj.actions.duplicate

import com.github.strindberg.emacsj.duplicate.DuplicateAndCommentHandler
import com.github.strindberg.emacsj.duplicate.Type
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CommentDwimAction : TextComponentEditorAction(DuplicateAndCommentHandler(Type.DWIM))
