package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.search.History
import com.intellij.openapi.components.Service
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.VisibleForTesting

/**
 * The saved mark positions of one project, a stack per file.
 */
@Service(Service.Level.PROJECT)
internal class MarkPlaces {

    private val places = mutableMapOf<String, History<PlaceInfo>>()

    internal fun push(file: VirtualFile, place: PlaceInfo) {
        places.getOrPut(file.signature()) { History() }.push(place)
    }

    internal fun peek(file: VirtualFile): PlaceInfo? = places[file.signature()]?.peek()

    internal fun pop(file: VirtualFile): PlaceInfo? = places[file.signature()]?.pop()

    @VisibleForTesting
    internal fun clear() {
        places.clear()
    }
}

private fun VirtualFile.signature(): String = fileSystem.protocol + path
