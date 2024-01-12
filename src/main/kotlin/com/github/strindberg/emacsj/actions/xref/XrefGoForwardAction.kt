package com.github.strindberg.emacsj.actions.xref

import com.github.strindberg.emacsj.xref.Type
import com.github.strindberg.emacsj.xref.XrefHandler
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.openapi.project.DumbAware

class XrefGoForwardAction : GotoDeclarationAction(), DumbAware {

    private val actionHandler = XrefHandler(Type.GO_FORWARD)
    override fun getHandler(): CodeInsightActionHandler = actionHandler
}
