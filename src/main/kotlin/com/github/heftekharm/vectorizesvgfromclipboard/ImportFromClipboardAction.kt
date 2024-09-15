package com.github.heftekharm.vectorizesvgfromclipboard

import com.android.ide.common.vectordrawable.Svg2Vector
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiDirectoryImpl
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter


class ImportFromClipboardAction:AnAction() {
    private lateinit var virtualFileRes: VirtualFile

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val data = Toolkit.getDefaultToolkit()
            .systemClipboard.getData(DataFlavor.stringFlavor) as? String
        data?.takeIf { it.startsWith("<svg") }
        if(data==null){
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Svg Importer")
                .createNotification("There is no valid svg in the clipboard", NotificationType.INFORMATION)
                .notify(project)
            return
        }


        val resPath = File(virtualFileRes.path)

        val dialog = ImportDialogWrapper()
        val result = dialog.showAndGet()

        if(result){
            val tempInputFile = File.createTempFile("in_temp_svg" , System.currentTimeMillis().toString())
            BufferedWriter(FileWriter(tempInputFile)).write(data)
            val output = File(resPath , "/drawables/" + dialog.name + ".xml")
            Svg2Vector.parseSvgToXml(tempInputFile.toPath() ,  FileOutputStream(output))

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