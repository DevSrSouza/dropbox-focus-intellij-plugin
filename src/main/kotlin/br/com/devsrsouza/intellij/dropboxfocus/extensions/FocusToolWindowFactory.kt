package br.com.devsrsouza.intellij.dropboxfocus.extensions

import androidx.compose.ui.awt.ComposePanel
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettingsReader
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.ui.FocusSelection
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import java.awt.Dimension

class FocusToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ApplicationManager.getApplication().invokeLater {
            toolWindow.component.add(
                ComposePanel().apply {
                    val focusService = project.service<FocusService>()
                    preferredSize = Dimension(300, 300)
                    setContent {
                        val gradleSettingsState = project.service<FocusGradleSettingsReader>()
                            .focusGradleSettings
                        FocusSelection(
                            currentFocusGradleSettingsState = gradleSettingsState,
                            isLoadingState = focusService.focusOperationState,
                            syncGradle = focusService::syncGradle,
                            selectModuleToFocus = { focusGradleSettings, focusModule ->
                                focusService.focusOn(focusGradleSettings, focusModule.gradleModulePath)
                            },
                            clearFocus = {
                                focusService.clearFocus(
                                    focusSettings = gradleSettingsState.value!!,
                                    requireSync = true,
                                )
                            },
                            withClearFocusButton = true,
                        )
                    }
                }
            )
        }
    }

    override fun isApplicable(project: Project): Boolean {
        return true
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return project.service<FocusGradleSettingsReader>().focusGradleSettings.value != null
    }
}