package br.com.devsrsouza.intellij.dropboxfocus.services

import br.com.devsrsouza.intellij.dropboxfocus.ui.FocusSelectionComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.layout.panel
import javax.swing.border.EmptyBorder

@Service
class FocusToolWindowService(private val project: Project) {
    private var toolWindow: ToolWindow? = null
    private var focusSelectionComponent: FocusSelectionComponent? = null

    fun init() {
        project.messageBus
            .connect(project)
            .subscribe(
                FocusGradleSettingsListener.TOPIC,
                object : FocusGradleSettingsListener {
                    override fun gradleSettingsChanged(gradleSettings: FocusGradleSettings?) {
                        createOrReload(gradleSettings)
                    }
                }
            )
    }

    fun createOrReload(focusGradleSettings: FocusGradleSettings?) {
        if (toolWindow == null && focusGradleSettings != null) {
            toolWindow = registerToolWindow(focusGradleSettings)
        }

        if (toolWindow != null) {
            focusSelectionComponent?.update(focusGradleSettings)
            if (toolWindow!!.isAvailable && focusGradleSettings == null) {
                toolWindow!!.remove()
                toolWindow = null
            }
        }
    }

    private fun registerToolWindow(focusGradleSettings: FocusGradleSettings): ToolWindow {
        return ToolWindowManager.getInstance(project).registerToolWindow(
            RegisterToolWindowTask(
                id = "Focus",
                anchor = ToolWindowAnchor.LEFT,
                canCloseContent = false,
                component = panel {
                    focusSelectionComponent = FocusSelectionComponent(
                        focusGradleSettings,
                        project.service(),
                    ).apply {
                        render(this@panel)
                    }
                }.apply {
                    border = EmptyBorder(10, 10, 10, 10)
                },
                sideTool = true,
            )
        )
    }
}