package com.github.strindberg.emacsj

import com.intellij.DynamicBundle
import com.intellij.openapi.util.NlsActions
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

object EmacsJBundle {
    @NonNls
    const val EMACSJ_BUNDLE = "messages.EmacsJBundle"

    private val instance = DynamicBundle(EmacsJBundle::class.java, EMACSJ_BUNDLE)

    @Nls
    internal fun message(@PropertyKey(resourceBundle = EMACSJ_BUNDLE) key: String): String? = instance.getMessage(key)

    @NlsActions.ActionText
    internal fun actionText(@NonNls actionId: String): String? = message("action.$actionId.text")
}
