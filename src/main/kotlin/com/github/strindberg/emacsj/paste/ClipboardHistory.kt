package com.github.strindberg.emacsj.paste

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import com.intellij.openapi.ide.CopyPasteManager

internal fun clipboardHistory(): List<Transferable> =
    CopyPasteManager.getInstance().allContents
        .filter { it.isDataFlavorSupported(DataFlavor.stringFlavor) && it.asText().isNotBlank() }
        .distinctBy { it.asText() }

internal fun clipboardHistoryTexts(): List<String> = clipboardHistory().map { it.asText() }

internal fun Transferable.asText(): String = getTransferData(DataFlavor.stringFlavor) as String
