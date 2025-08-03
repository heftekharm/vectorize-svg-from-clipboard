package com.github.heftekharm.vectorizesvgfromclipboard

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

object Utils {

    fun showNotification(project: Project, message: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("com.hfm.importfromclipboard.notification")
            .createNotification(message, type)
            .notify(project)
    }

    fun getSvgContentFromClipboard(): String? {
        val svgPattern = Regex("<svg.*?>.*?</svg>", RegexOption.DOT_MATCHES_ALL)

        val systemClipboard=Toolkit.getDefaultToolkit().systemClipboard
        val matchedSvgContent = runCatching {(systemClipboard.getData(DataFlavor.stringFlavor) as? String)?.let { svgPattern.find(it) }}.getOrNull() ?:
        runCatching {(systemClipboard.getContents(null).getTransferData(DataFlavor.javaFileListFlavor) as? List<File>)?.first()?.readText()
            ?.let { svgPattern.find(it) }}.getOrNull()

        return matchedSvgContent?.value
    }


}