package com.github.heftekharm.vectorizesvgfromclipboard

import com.android.tools.idea.npw.assetstudio.wizard.GenerateIconsModel
import com.android.tools.idea.projectsystem.AndroidModulePaths
import com.android.tools.idea.projectsystem.NamedModuleTemplate
import com.android.tools.idea.projectsystem.getModuleSystem
import com.android.tools.idea.wizard.model.ModelWizard
import com.android.tools.idea.wizard.ui.StudioWizardDialogBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import org.jetbrains.android.facet.AndroidFacet
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File


class ImportFromClipboardAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }


    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val clipboardData = Toolkit.getDefaultToolkit()
            .systemClipboard.getData(DataFlavor.stringFlavor) as? String
        val svgPattern = Regex("<svg.*?>.*?</svg>", RegexOption.DOT_MATCHES_ALL)
        val matchedSvg = clipboardData?.let { svgPattern.find(it) } ?: run {
            showNotification(project, "There is no valid svg in the clipboard")
            return
        }

        val tempInputFile = File.createTempFile("temp_svg_from_clipboard_file_", ".svg").apply {
            writeText(matchedSvg.value)
        }

        val dataContext: DataContext = anActionEvent.dataContext

        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return

        val module = PlatformCoreDataKeys.MODULE.getData(dataContext) ?: return

        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        val facet = AndroidFacet.getInstance(location, project) ?: return

        val template = getModuleTemplate(module, location) ?: return

        val resFolder = findClosestResFolder(template.paths, location) ?: return

        val wizard: ModelWizard = ModelWizard.Builder()
            .addStep(
                SvgFromClipboardAssetStep(
                    GenerateIconsModel(facet, "vectorWizard", template, resFolder),
                    facet,
                    tempInputFile
                )
            ).build()

        val dialogBuilder = StudioWizardDialogBuilder(wizard, "Svg From Clipboard")
        dialogBuilder.setProject(facet.getModule().getProject())
            .setMinimumSize(JBUI.size(700, 540))
            .setPreferredSize(JBUI.size(700, 540))
        dialogBuilder.build().show()
        tempInputFile.delete()
    }

    override fun update(anActionEvent: AnActionEvent) {

        //val startTime = System.currentTimeMillis()

        val dataContext: DataContext = anActionEvent.dataContext

        val module = PlatformCoreDataKeys.MODULE.getData(dataContext) ?: return

        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        val paths = module.getModuleSystem().getModuleTemplates(location).firstOrNull { it.name == "main" }?.paths

        val locationPath = location.presentableUrl

        val isVisible = paths?.moduleRoot?.path == locationPath || paths?.resDirectories?.any {
            val path = it.path
            locationPath == path || locationPath.startsWith("$path${File.separator}drawable")
        } == true
        anActionEvent.presentation.isEnabledAndVisible = isVisible

        //val endTime = System.currentTimeMillis()
        //showNotification(anActionEvent.project!! , "time is:" + (endTime - startTime) )
    }

    private fun getModuleTemplate(module: Module, location: VirtualFile): NamedModuleTemplate? {
        for (namedTemplate in module.getModuleSystem().getModuleTemplates(location)) {
            if (namedTemplate.paths.resDirectories.isNotEmpty()) {
                return namedTemplate
            }
        }
        return null
    }

    private fun findClosestResFolder(paths: AndroidModulePaths, location: VirtualFile): File? {
        val toFind = location.path
        var bestMatch: File? = null
        var bestCommonPrefixLength = -1
        for (resDir in paths.resDirectories) {
            val commonPrefixLength = StringUtil.commonPrefixLength(resDir.path, toFind)
            if (commonPrefixLength > bestCommonPrefixLength) {
                bestCommonPrefixLength = commonPrefixLength
                bestMatch = resDir
            }
        }
        return bestMatch
    }


    private fun showNotification(project: Project, message: String){
        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.hfm.importfromclipboard.notification")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}