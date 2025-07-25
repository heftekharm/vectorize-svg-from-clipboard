package com.github.heftekharm.vectorizesvgfromclipboard

import com.android.tools.idea.model.StudioAndroidModuleInfo
import com.android.tools.idea.projectsystem.AndroidModulePaths
import com.android.tools.idea.projectsystem.NamedModuleTemplate
import com.android.tools.idea.projectsystem.getModuleSystem
import com.android.tools.idea.util.toIoFile
import com.android.tools.idea.wizard.model.ModelWizard
import com.android.tools.idea.wizard.ui.StudioWizardDialogBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
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
        val svgPattern = Regex("<svg.*?>.*?</svg>", RegexOption.DOT_MATCHES_ALL)
        val matchedSvg = kotlin.runCatching {
            Toolkit.getDefaultToolkit()
                .systemClipboard?.run {
                    (getData(DataFlavor.stringFlavor) as? String)?.let { svgPattern.find(it) }
                        ?: (getContents(null).getTransferData(DataFlavor.javaFileListFlavor) as? List<File>)?.first()?.readText()
                            ?.let { svgPattern.find(it) }
                }?.value
        }.getOrNull() ?: run {
            showNotification(project, "There is no valid svg in the clipboard")
            return
        }

        val tempInputFile = File.createTempFile("temp_svg_from_clipboard_file_", ".svg").apply {
            writeText(matchedSvg)
        }

        val dataContext: DataContext = anActionEvent.dataContext

        val module = PlatformCoreDataKeys.MODULE.getData(dataContext) ?: return

        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return

        val facet = AndroidFacet.getInstance(location, project)

        var template = getModuleTemplate(module, location)

        var resFolder = template?.paths?.let { findClosestResFolder(it, location) }

        val isAndroidModule = template != null

        if (isAndroidModule.not()) {
            val kmpResFolder = module.rootManager.contentRoots.firstOrNull { it.isDirectory && it.name == module.name.split(".").last() }?.findChild("composeResources") ?.toIoFile()
            template = NamedModuleTemplate(
                "fake", FakeModulePaths(kmpResFolder!!)
            )
            resFolder = kmpResFolder
        }

        if (resFolder == null) {
            showNotification(project, "Could not find Resource folder")
            return
        }

        val minSdk = facet?.let {   StudioAndroidModuleInfo.getInstance(it).minSdkVersion.apiLevel} ?: 0

        val wizard: ModelWizard = ModelWizard.Builder()
            .addStep(
                SvgFromClipboardAssetStep(
                    CustomGenerateIconsModel(project, "vectorWizard", template!!, resFolder),
                    project,
                    minSdk,
                    tempInputFile
                )
            ).build()

        val dialogBuilder = StudioWizardDialogBuilder(wizard, "Svg From Clipboard")
        dialogBuilder.setProject(project)
            .setMinimumSize(JBUI.size(700, 540))
            .setPreferredSize(JBUI.size(800, 540))
        dialogBuilder.build().show()
        tempInputFile.delete()
    }

    override fun update(anActionEvent: AnActionEvent) {
        val dataContext: DataContext = anActionEvent.dataContext
        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return
        val isVisible = location.takeIf { it.isDirectory }?.name?.let {
            it.startsWith("drawable") ||  it in arrayOf("res" , "composeResources" , "commonMain" , "app")
        } ?: false
        anActionEvent.presentation.isEnabledAndVisible = isVisible
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