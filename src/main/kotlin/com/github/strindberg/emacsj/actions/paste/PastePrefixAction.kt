package com.github.strindberg.emacsj.actions.paste

import com.github.strindberg.emacsj.paste.PasteHandler
import com.github.strindberg.emacsj.paste.Type
import com.intellij.openapi.editor.actionSystem.EditorAction

class PastePrefixAction : EditorAction(PasteHandler(Type.PREFIX))
