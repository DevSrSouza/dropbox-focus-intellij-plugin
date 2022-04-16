package br.com.devsrsouza.intellij.dropboxfocus.services

import com.intellij.application.options.editor.checkBox
import com.intellij.openapi.components.service
import com.intellij.openapi.observable.properties.GraphPropertyImpl.Companion.graphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.toBinding
import com.intellij.ui.layout.withSelectedBinding
import javax.swing.JComponent

class FocusConfigurable(private val project: Project) : SearchableConfigurable {

    private val settings = project.service<FocusSettings>()
    private val propertyGraph = PropertyGraph()
    private val shouldShowStartupDialog = propertyGraph.graphProperty { settings.shouldShowStartupDialog }

    private val settingsPanel = panel {
        row {
            checkBox(
                "Show Focus startup dialog for this project",
                shouldShowStartupDialog
            )
                .withSelectedBinding(settings::shouldShowStartupDialog.toBinding())
        }
    }

    override fun createComponent(): JComponent = settingsPanel

    override fun isModified(): Boolean = settingsPanel.isModified()

    override fun apply() {
        settingsPanel.apply()
    }

    override fun reset() = settingsPanel.reset()

    override fun getDisplayName(): String = "Dropbox Focus"

    override fun getId(): String = ID

    companion object {
        const val ID = "br.com.devsrsouza.intellij.dropboxfocus.configurable"
    }
}