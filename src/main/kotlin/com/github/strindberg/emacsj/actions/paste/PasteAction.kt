package com.github.strindberg.emacsj.actions.paste

import com.github.strindberg.emacsj.paste.PasteHandler
import com.github.strindberg.emacsj.paste.PasteType
import com.intellij.openapi.editor.actionSystem.EditorAction

class PasteAction : EditorAction(PasteHandler(PasteType.STANDARD))
