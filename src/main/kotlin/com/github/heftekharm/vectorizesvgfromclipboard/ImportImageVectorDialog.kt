package com.github.heftekharm.vectorizesvgfromclipboard

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField


class ImportImageVectorDialog(private val initialFileName: String) : DialogWrapper(true) {

    private val properties = PropertiesComponent.getInstance()
    //private val previousAccessor = properties.getValue("myplugin.lastAccessor", "Icons")

    private lateinit var fileNameField: JTextField
    //private lateinit var accessorNameField: JTextField

    init {
        title = "Import Svg As ImageVector"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("File Name:") {
                textField().let {
                    fileNameField = it.component
                    fileNameField.text = initialFileName
                }
            }
            /*row("Accessor Name:") {
                textField().let {
                    accessorNameField = it.component
                    accessorNameField.text = previousAccessor
                }
            }*/
        }
    }

    override fun doValidate(): ValidationInfo? {
        if (fileNameField.text.trim().isEmpty()) {
            return ValidationInfo("File name cannot be empty", fileNameField)
        }
        /*if (accessorNameField.text.trim().isEmpty()) {
            return ValidationInfo("Accessor name cannot be empty", accessorNameField)
        }*/
        return null
    }

    override fun doOKAction() {
        //properties.setValue("myplugin.lastAccessor", accessorNameField.text.trim())
        super.doOKAction()
        dispose()
    }

    fun getFileName(): String = fileNameField.text.trim()
    fun getAccessorName(): String = "MyDefaultVectorImagesAccessor" //accessorNameField.text.trim()


    companion object {
        const val DEFAULT_ACCESSOR_NAME = "MyDefaultVectorImagesAccessor"
    }
}