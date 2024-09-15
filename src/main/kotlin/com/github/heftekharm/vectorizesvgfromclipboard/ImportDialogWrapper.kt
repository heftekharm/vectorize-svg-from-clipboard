package com.github.heftekharm.vectorizesvgfromclipboard

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class ImportDialogWrapper: DialogWrapper(true) {

    init {
        init()
        title = "Svg Importer"
    }

    lateinit var dialog:ImportDialog
    lateinit var name:String
        private set


    override fun createCenterPanel(): JComponent? {
        dialog = ImportDialog()
        return dialog.panel
    }

    override fun doOKAction() {
        name = dialog.nameTextField.text
        super.doOKAction()
    }
}