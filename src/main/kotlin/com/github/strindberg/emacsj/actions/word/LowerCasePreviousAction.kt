package com.github.strindberg.emacsj.actions.word

import com.github.strindberg.emacsj.word.ChangeType
import com.github.strindberg.emacsj.word.WordChangeHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class LowerCasePreviousAction : TextComponentEditorAction(WordChangeHandler(ChangeType.LOWER_PREVIOUS))
