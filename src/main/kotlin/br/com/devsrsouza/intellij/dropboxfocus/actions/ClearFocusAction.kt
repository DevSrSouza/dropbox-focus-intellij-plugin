package br.com.devsrsouza.intellij.dropboxfocus.actions

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettingsReader
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ClearFocusAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val focusSettings = project?.service<FocusGradleSettingsReader>()?.getProjectFocusSettings() ?: return

        if (focusSettings.currentFocusModulePath != null) {
            project.service<FocusService>().clearFocus(focusSettings, requireSync = true)
        }
    }

    override fun update(event: AnActionEvent) {
        val project = event.project
        val focusSettings = project?.service<FocusGradleSettingsReader>()?.getProjectFocusSettings()

        event.presentation.isEnabled = focusSettings?.currentFocusModulePath != null
    }
}