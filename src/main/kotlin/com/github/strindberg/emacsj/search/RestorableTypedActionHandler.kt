package com.github.strindberg.emacsj.search

import com.intellij.codeInsight.template.impl.editorActions.TypedActionHandlerBase
import com.intellij.openapi.editor.actionSystem.TypedActionHandler

internal abstract class RestorableTypedActionHandler(val originalHandler: TypedActionHandler) : TypedActionHandlerBase(originalHandler)
