package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.CopyRegionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CopyRegionAction : TextComponentEditorAction(CopyRegionHandler())
