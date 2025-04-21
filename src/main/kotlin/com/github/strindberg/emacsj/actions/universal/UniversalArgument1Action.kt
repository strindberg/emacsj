package com.github.strindberg.emacsj.actions.universal

import com.github.strindberg.emacsj.universal.UniversalArgumentHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class UniversalArgument1Action : TextComponentEditorAction(UniversalArgumentHandler(1))
