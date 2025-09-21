package com.github.strindberg.emacsj.actions.universal

import com.github.strindberg.emacsj.actions.repeat.RepeatAction
import com.github.strindberg.emacsj.universal.CancelRepeatHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class CancelRepeatAction :
    EditorAction(CancelRepeatHandler()),
    RepeatAction
