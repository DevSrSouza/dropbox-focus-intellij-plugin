package br.com.devsrsouza.intellij.dropboxfocus.listeners

import br.com.devsrsouza.intellij.dropboxfocus.actions.logger
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettingsReader
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusToolWindowService
import br.com.devsrsouza.intellij.dropboxfocus.ui.dialog.StartupFocusProjectDialog
import com.android.tools.idea.gradle.project.sync.GradleSyncState
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class FocusProjectOpenListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        GradleSyncState.subscribe(project, createFocusGradleSyncListener())
        project.service<FocusToolWindowService>().init()

        val shouldShowDialog = project.service<FocusSettings>().shouldShowStartupDialog
        if (shouldShowDialog.not()) return

        val settingsService = project.service<FocusGradleSettingsReader>()

        val focusSettings = settingsService.getProjectFocusSettings()
        logger.debug("Founded Focus Settings in ${project.name}: $focusSettings")

        if (focusSettings != null) {
            val focusService = project.service<FocusService>()
            StartupFocusProjectDialog(project, focusSettings, focusService).show()
        }
    }
}