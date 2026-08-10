package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.PlaceInfo
import com.intellij.openapi.components.Service

/**
 * The declaration-navigation history of one project.
 */
@Service(Service.Level.PROJECT)
internal class XRefPlaces {

    internal val stack = UndoRedoStack<PlaceInfo>()
}
