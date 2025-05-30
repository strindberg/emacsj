package com.github.strindberg.emacsj.preferences

import javax.swing.Icon
import com.github.strindberg.emacsj.search.EMACSJ_PRIMARY
import com.github.strindberg.emacsj.search.EMACSJ_SECONDARY
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.psi.tree.IElementType

internal class EmacsJColorSettingsPage : ColorSettingsPage {

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
        arrayOf(
            AttributesDescriptor("Primary search match", EMACSJ_PRIMARY),
            AttributesDescriptor("Secondary search matches", EMACSJ_SECONDARY),
        )

    override fun getDisplayName(): String = "EmacsJ"

    override fun getHighlighter(): SyntaxHighlighter =
        object : SyntaxHighlighterBase() {
            override fun getHighlightingLexer(): Lexer = EmacsJLexer()

            override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
                when (tokenType) {
                    PRIMARY_TOKEN_TYPE -> arrayOf(EMACSJ_PRIMARY)
                    SECONDARY_TOKEN_TYPE -> arrayOf(EMACSJ_SECONDARY)
                    else -> arrayOf(DefaultLanguageHighlighterColors.IDENTIFIER)
                }
        }

    override fun getDemoText(): String =
        """
        |Below, the first search match is the primary match, the following are secondary:
        |While searching, the primary result is the one where the caret is positioned.
        |Other results are indicated as a secondary result.
        """.trimMargin()

    override fun getIcon(): Icon? = null

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
}
