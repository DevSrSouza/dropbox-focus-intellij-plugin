package br.com.devsrsouza.intellij.dropboxfocus.listeners

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettingsReader
import com.android.tools.idea.gradle.project.sync.GradleSyncListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class FocusGradleSyncListener : GradleSyncListener {
    override fun syncSucceeded(project: Project) {
        project.service<FocusGradleSettingsReader>()
            .reloadProjectFocusSettings()
    }
}