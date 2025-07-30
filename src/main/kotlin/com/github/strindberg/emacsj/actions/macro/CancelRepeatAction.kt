package com.github.strindberg.emacsj.actions.macro

import com.github.strindberg.emacsj.macro.CancelRepeatHandler
import com.intellij.openapi.editor.actionSystem.EditorAction

class CancelRepeatAction :
    EditorAction(CancelRepeatHandler()),
    RepeatAction
