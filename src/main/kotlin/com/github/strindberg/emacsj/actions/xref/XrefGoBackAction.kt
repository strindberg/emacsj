package com.github.strindberg.emacsj.actions.xref

import com.github.strindberg.emacsj.xref.Type
import com.github.strindberg.emacsj.xref.XrefHandler
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.project.DumbAware

class XrefGoBackAction : BaseCodeInsightAction(), DumbAware {

    private val actionHandler = XrefHandler(Type.GO_BACK)
    override fun getHandler(): CodeInsightActionHandler = actionHandler
}
