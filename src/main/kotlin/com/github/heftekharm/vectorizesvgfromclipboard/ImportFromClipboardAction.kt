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
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.file.PsiDirectoryImpl
import com.intellij.util.ui.JBUI
import org.jetbrains.android.facet.AndroidFacet
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.net.URL


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
        //val dialog = ImportDialogWrapper()
        //val result = dialog.showAndGet()
        val tempInputFile = File.createTempFile("in_temp_svg", System.currentTimeMillis().toString()).apply {
            writeText(matchedSvg.value)
        }

        val dataContext: DataContext = anActionEvent.dataContext

        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return

        val module = PlatformCoreDataKeys.MODULE.getData(dataContext) ?: return

        val location = CommonDataKeys.VIRTUAL_FILE.getData(dataContext) ?: return


        val facet = AndroidFacet.getInstance(virtualFileRes, project) ?: return

        val template = getModuleTemplate(module, location) ?: return

        //val asset = SVGImporter().processFile(tempInputFile)

        val resFolder = findClosestResFolder(template.paths, location) ?: return

        val wizard: ModelWizard = ModelWizard.Builder()
            .addStep(
                SvgFromClipboardAssetStep(
                    GenerateIconsModel(facet, "vectorWizard", template, resFolder),
                    facet
                )
            ).build()

        val dialogBuilder = StudioWizardDialogBuilder(wizard, "Svg From Clipboard")
        dialogBuilder.setProject(facet.getModule().getProject())
            .setMinimumSize(JBUI.size(700, 540))
            .setPreferredSize(JBUI.size(700, 540))
            .setHelpUrl(URL("http://developer.android.com/tools/help/vector-asset-studio.html"))
        dialogBuilder.build().show()

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

    private fun getModuleTemplate(module: Module, location: VirtualFile): NamedModuleTemplate? {
        for (namedTemplate in module.getModuleSystem().getModuleTemplates(location)) {
            if (!namedTemplate.paths.resDirectories.isEmpty()) {
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
}