package com.github.strindberg.emacsj.xref

import com.github.strindberg.emacsj.mark.LimitedStack
import com.github.strindberg.emacsj.mark.PlaceInfoWrapper
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.navigation.actions.GotoDeclarationOrUsageHandler2
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

enum class Type { GO_FORWARD, GO_BACK }

class XrefHandler(val type: Type) : CodeInsightActionHandler {

    override fun startInWriteAction(): Boolean = GotoDeclarationOrUsageHandler2.startInWriteAction()

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        (editor as? EditorEx)?.let { editorEx ->
            when (type) {
                Type.GO_FORWARD -> {
                    val cursorPos = PlaceInfoWrapper.placeInfo(editorEx, editorEx.caretModel.offset, file.virtualFile)
                    if (cursorPos != null && history.peek() != cursorPos) {
                        history.push(cursorPos)
                    }
                    GotoDeclarationOrUsageHandler2.invoke(project, editor, file)
                }
                Type.GO_BACK -> {
                    history.pop()?.let { cursorPos ->
                        IdeDocumentHistory.getInstance(project).gotoPlaceInfo(cursorPos.placeInfo)
                    }
                }
            }
        }
    }

    companion object {
        private val history: LimitedStack<PlaceInfoWrapper> = LimitedStack()
    }
}
