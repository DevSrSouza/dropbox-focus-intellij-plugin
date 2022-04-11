package br.com.devsrsouza.intellij.dropboxfocus.listeners

import br.com.devsrsouza.intellij.dropboxfocus.actions.logger
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettingsReader
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.ui.DialogWrapper
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.*

internal class FocusProjectOpenListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        val settingsService = project.service<FocusSettingsReader>()

        val focusSettings = settingsService.getProjectFocusSettings()
        logger.debug("Founded Focus Settings in ${project.name}: $focusSettings")

        if (focusSettings != null) {
            val focusService = project.service<FocusService>()
            selectFocusDialog(focusSettings, focusService)
                .showAndGet()
        }
    }

    // TODO: Migrate to Compose
    private fun selectFocusDialog(focusSettings: FocusSettings, focusService: FocusService): DialogWrapper =
        object : DialogWrapper(true) {
            init {
                title = "Focus"
                init()
            }

            override fun createCenterPanel(): JComponent? {
                return JPanel().apply {
                    layout = GridLayout(0, 1)
                    if (focusSettings.currentFocus != null) {
                        add(JLabel("Current focus: ${focusSettings.currentFocus}"))
                    }
                    add(
             JLabel("Select project to Focus"))

                    for ((modulePath, _) in focusSettings.allModules) {
                        val button = JButton(modulePath).apply {
                            addActionListener {
                                close(0)
                                focusService.focusOn(focusSettings, modulePath)
                            }
                        }

                        add(button)
                    }
                }
            }
        }
}
