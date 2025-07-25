package com.github.heftekharm.vectorizesvgfromclipboard

import br.com.devsrsouza.svg2compose.Svg2Compose
import com.android.ide.common.util.toPathString
import com.android.ide.common.util.toPathStringOrNull
import com.android.tools.idea.util.toIoFile
import com.android.utils.FileUtils
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.File


class ImportSvgFromClipboardAsImageVectorAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }


    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val matchedSvg = Utils.getSvgContentFromClipboard() ?: run {
            Utils.showNotification(project, "There is no valid svg in the clipboard")
            return
        }
        val dataContext: DataContext = anActionEvent.dataContext
        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return
        val initialFileName = "Icon${System.currentTimeMillis()}"
        val dialog = ImportImageVectorDialog(initialFileName)
        if(dialog.showAndGet()){
            generate(
                svgContent =  matchedSvg ,
                location = location ,
                fileName = dialog.getFileName() ,
                accessorName = dialog.getAccessorName()
                )
        }


    }


    private fun generate(svgContent: String, location: VirtualFile, fileName: String, accessorName: String) {
        val directoryPackage = getPackageOfFolder(location)

        val tempInputFolder = File(FileUtil.getTempDirectory() , "inivectors")
        if (!tempInputFolder.exists()) {
            FileUtil.createDirectory(tempInputFolder)
        }
        FileUtils.cleanOutputDir(tempInputFolder)

        val tempInputFile = FileUtil.createTempFile(tempInputFolder, fileName, ".svg").apply {
            writeText(svgContent)
        }

        val tempOutputFolder = File(FileUtil.getTempDirectory() , "outivectors")

        if (!tempOutputFolder.exists()) {
            FileUtil.createDirectory(tempOutputFolder)
        }
        FileUtils.cleanOutputDir(tempOutputFolder)

        Svg2Compose.parse(
            applicationIconPackage = directoryPackage,
            accessorName = accessorName,
            outputSourceDirectory = tempOutputFolder,
            vectorsDirectory = tempInputFolder,
        )

        val generatedContentFolder = tempOutputFolder.resolve(directoryPackage.replace(".", File.separator))

        FileUtil.copyDirContent(generatedContentFolder, location.toIoFile())
        location.refresh(true, true)
    }

    override fun update(anActionEvent: AnActionEvent) {
        val dataContext: DataContext = anActionEvent.dataContext
        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        val isVisible = location.takeIf { it.isDirectory }?.toIoFile().toPathStringOrNull()?.segments?.contains("res")?.not() ?: false
        anActionEvent.presentation.isEnabledAndVisible = isVisible
    }


    private fun getPackageOfFolder(location: VirtualFile): String {
        var kotlinOrJavaFolderIndex = -1
        val pathSegments = location.toIoFile().toPathString().segments
        for (i in pathSegments.lastIndex downTo 0) {
            val segment = pathSegments[i]
            if (segment == "kotlin" || segment == "java")
                kotlinOrJavaFolderIndex = i
            if (segment == "src")
                break
        }
        return if (kotlinOrJavaFolderIndex >= 0)
            pathSegments.drop(kotlinOrJavaFolderIndex + 1).joinToString(".")
        else
            "NotFoundPackage"
    }


}