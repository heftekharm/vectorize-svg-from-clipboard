package com.github.heftekharm.vectorizesvgfromclipboard

import com.intellij.openapi.ui.DialogWrapper
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class ImportDialogWrapper: DialogWrapper(true) {

    init {
        init()
        title = "Svg Importer"
    }

    //lateinit var dialog:ImportDialog
    lateinit var name:String
        private set

    lateinit var nameField: JTextField


    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(GridLayout(3, 2, 10, 10))


        // Name input
        val nameLabel = JLabel("Name:")
        nameField = JTextField()
        dialogPanel.add(nameLabel)
        dialogPanel.add(nameField)


        // Width input
        val widthLabel = JLabel("Width:")
        val widthField = JTextField()
        dialogPanel.add(widthLabel)
        dialogPanel.add(widthField)


        // Height input
        val heightLabel = JLabel("Height:")
        val heightField = JTextField()
        dialogPanel.add(heightLabel)
        dialogPanel.add(heightField)

        return dialogPanel



        //dialog = ImportDialog()
        //return dialogPanel
    }

    override fun doOKAction() {
        name = nameField.text
        super.doOKAction()
    }
}