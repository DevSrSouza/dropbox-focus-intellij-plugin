package br.com.devsrsouza.intellij.dropboxfocus.listeners

import br.com.devsrsouza.intellij.dropboxfocus.actions.logger
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettingsReader
import br.com.devsrsouza.intellij.dropboxfocus.ui.StartupFocusProjectDialog
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class FocusProjectOpenListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        val shouldShowDialog = project.service<FocusSettings>().shouldShowStartupDialog
        if (shouldShowDialog.not()) return

        val settingsService = project.service<FocusSettingsReader>()

        val focusSettings = settingsService.getProjectFocusSettings()
        logger.debug("Founded Focus Settings in ${project.name}: $focusSettings")

        if (focusSettings != null) {
            val focusService = project.service<FocusService>()
            StartupFocusProjectDialog(project, focusSettings, focusService).show()
        }
    }
}