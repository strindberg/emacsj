package com.github.strindberg.emacsj.actions.kill

import com.github.strindberg.emacsj.kill.CutRegionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class CutRegionAction : TextComponentEditorAction(CutRegionHandler())
