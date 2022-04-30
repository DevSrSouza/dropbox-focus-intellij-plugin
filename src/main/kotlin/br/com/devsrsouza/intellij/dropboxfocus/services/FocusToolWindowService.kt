package br.com.devsrsouza.intellij.dropboxfocus.services

import androidx.compose.ui.awt.ComposePanel
import br.com.devsrsouza.intellij.dropboxfocus.ui.FocusSelection
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.Dimension

@Service
class FocusToolWindowService(private val project: Project) : Disposable {
    private val job = Job()
    private var toolWindow: ToolWindow? = null

    fun init() {
        GlobalScope.launch(Dispatchers.Swing + job) {
            project.service<FocusGradleSettingsReader>().focusGradleSettings.collect {
                createOrReload(it)
            }
        }
    }

    private fun createOrReload(focusGradleSettings: FocusGradleSettings?) {
        ToolWindowManager.getInstance(project).invokeLater {
            if (toolWindow == null && focusGradleSettings != null) {
                toolWindow = registerToolWindow()
            }

            if (toolWindow != null) {
                if (toolWindow!!.isAvailable && focusGradleSettings == null) {
                    toolWindow!!.remove()
                    toolWindow = null
                }
            }
        }
    }

    private fun registerToolWindow(): ToolWindow {
        return ToolWindowManager.getInstance(project).registerToolWindow(
            RegisterToolWindowTask(
                id = "Focus",
                anchor = ToolWindowAnchor.LEFT,
                canCloseContent = false,
                component = ComposePanel().apply {
                    val focusService = project.service<FocusService>()
                    preferredSize = Dimension(300, 300)
                    setContent {
                        FocusSelection(
                            currentFocusGradleSettingsState = project.service<FocusGradleSettingsReader>().focusGradleSettings,
                            isLoadingState = focusService.focusOperationState,
                            syncGradle = focusService::syncGradle,
                            selectModuleToFocus = { focusGradleSettings, focusModule ->
                                focusService.focusOn(focusGradleSettings, focusModule.gradleModulePath)
                            }
                        )
                    }
                },
                sideTool = true,
            )
        )
    }

    override fun dispose() {
        job.cancel()
    }
}