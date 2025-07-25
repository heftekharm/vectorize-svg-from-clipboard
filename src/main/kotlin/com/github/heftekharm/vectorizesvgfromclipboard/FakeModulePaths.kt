package com.github.heftekharm.vectorizesvgfromclipboard

import com.android.tools.idea.projectsystem.AndroidModulePaths
import java.io.File

class FakeModulePaths(resFolder: File) : AndroidModulePaths {
    override val manifestDirectory: File? = null
    override val moduleRoot: File? = null
    override val resDirectories: List<File> = listOf(resFolder)

    override fun getAidlDirectory(packageName: String?): File? = null

    override fun getSrcDirectory(packageName: String?): File? = null

    override fun getTestDirectory(packageName: String?): File? = null

    override fun getUnitTestDirectory(packageName: String?): File? = null
}