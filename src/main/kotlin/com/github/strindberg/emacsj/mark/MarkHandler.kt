package com.github.strindberg.emacsj.mark

import com.github.strindberg.emacsj.EmacsJService
import com.github.strindberg.emacsj.mark.MarkType.POP
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.lang.annotations.Language

enum class MarkType { PUSH, POP }

@Language("devkit-action-id")
internal const val ACTION_PUSH_MARK = "com.github.strindberg.emacsj.actions.mark.pushmark"

@Language("devkit-action-id")
internal const val ACTION_POP_MARK = "com.github.strindberg.emacsj.actions.mark.popmark"

internal class MarkHandler(private val type: MarkType) : EditorActionHandler() {

    companion object {

        internal fun pushPlaceInfo(editor: Editor) {
            editor.virtualFile?.let { virtualFile ->
                virtualFile.placeInfo(editor)?.let { placeInfo ->
                    editor.places()?.push(virtualFile, placeInfo)
                }
            }
        }

        internal fun peek(editor: Editor): PlaceInfo? =
            editor.virtualFile?.let { virtualFile ->
                editor.places()?.peek(virtualFile)
            }

        internal fun VirtualFile.placeInfo(editor: Editor): PlaceInfo? =
            editor.project?.manager?.getSelectedEditorWithProvider(this)?.let { editorWithProvider ->
                PlaceInfo(
                    file = this,
                    state = editorWithProvider.fileEditor.getState(FileEditorStateLevel.UNDO),
                    editorTypeId = editorWithProvider.provider.editorTypeId,
                    caretPosition = editor.caretModel.primaryCaret.offset,
                    scrollOffset = editor.scrollingModel.verticalScrollOffset,
                )
            }

        internal fun PlaceInfo.restore(editor: Editor) {
            editor.project?.manager?.let { manager ->
                manager.openFile(file, focusEditor = true)
                manager.setSelectedEditor(file, editorTypeId)
                manager.getSelectedEditorWithProvider(file)?.takeIf {
                    it.provider.editorTypeId == editorTypeId
                }?.let {
                    it.fileEditor.setState(state)
                    editor.scrollingModel.scrollVertically(scrollOffset)
                }
            }
        }

        private fun Editor.places(): MarkPlaces? = project?.service<MarkPlaces>()
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (editor is EditorEx) {
            editor.virtualFile?.let { virtualFile ->
                if (type == POP || EmacsJService.instance.isLastStrictUniversal()) {
                    editor.places()?.pop(virtualFile)?.restore(editor)
                } else {
                    val isPreviousSticky = editor.isStickySelection
                    editor.isStickySelection = false
                    virtualFile.placeInfo(editor)?.let { placeInfo ->
                        if (placeInfo != peek(editor) || !isPreviousSticky) {
                            editor.places()?.push(virtualFile, placeInfo)
                            editor.isStickySelection = true
                        }
                    }
                }
            }
        }
    }
}

internal val Project.manager: FileEditorManagerEx?
    get() = FileEditorManagerEx.getInstanceExIfCreated(this)

internal class PlaceInfo(
    val file: VirtualFile,
    val state: FileEditorState,
    val editorTypeId: String,
    val caretPosition: Int,
    val scrollOffset: Int,
) {
    override fun equals(other: Any?): Boolean =
        (other as? PlaceInfo)?.let {
            file == other.file && caretPosition == other.caretPosition
        } == true

    override fun hashCode(): Int = 31 * file.hashCode() + caretPosition.hashCode()
}
