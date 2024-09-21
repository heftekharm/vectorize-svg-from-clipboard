package com.github.heftekharm.vectorizesvgfromclipboard

import com.android.ide.common.vectordrawable.Svg2Vector
import com.android.tools.idea.ui.resourcemanager.plugin.VectorDrawableImporter
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiDirectoryImpl
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.FileOutputStream


class ImportFromClipboardAction : AnAction() {
    private lateinit var virtualFileRes: VirtualFile

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val clipboardData = Toolkit.getDefaultToolkit()
            .systemClipboard.getData(DataFlavor.stringFlavor) as? String
        val svgPattern = Regex("<svg.*?>.*?</svg>", RegexOption.DOT_MATCHES_ALL)
        val matchedSvg = clipboardData?.let { svgPattern.find(it) } ?: run {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("com.hfm.importfromclipboard.notification")
                .createNotification("There is no valid svg in the clipboard", NotificationType.INFORMATION)
                .notify(project)
            return
        }


        val resPath = File(virtualFileRes.path)
        val dialog = ImportDialogWrapper()
        val result = dialog.showAndGet()

        if (result) {
            val tempInputFile = File.createTempFile("in_temp_svg", System.currentTimeMillis().toString()).apply {
                writeText(matchedSvg.value)
            }
            val output = File(resPath, "/drawable/" + dialog.name + ".xml")
            //output.createNewFile()
            val outputStream = FileOutputStream(output)
            Svg2Vector.parseSvgToXml(tempInputFile.toPath(), outputStream)
        }

    }

    override fun update(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project
        val psiElement: PsiElement? = anActionEvent.dataContext.getData(PlatformDataKeys.PSI_ELEMENT)
        val isValid = project != null && psiElement != null &&
                (psiElement as? PsiDirectoryImpl)?.isDirectory == true &&
                (psiElement as? PsiDirectoryImpl)?.name == "res"
        anActionEvent.presentation.isEnabledAndVisible = isValid
        if (isValid) {
            virtualFileRes = (psiElement as PsiDirectoryImpl).virtualFile
        }
    }
}