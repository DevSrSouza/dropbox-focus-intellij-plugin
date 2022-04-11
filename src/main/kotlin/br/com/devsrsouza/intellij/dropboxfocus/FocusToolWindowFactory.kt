package br.com.devsrsouza.intellij.dropboxfocus

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class FocusToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//    val panel = panel() {
//      for(module in a ) {
//        row(module.moduleName) {
//
//        }
//      }
//    }
//
//    toolWindow.component.add(panel)
    }

    override fun isApplicable(project: Project): Boolean {
//    val focusSettings = project.service<FocusSettingsReader>().getProjectFocusSettings() ?: return false
//    project.allModules()
//    focusSettings.allModules
        // GradleLocalSettings.getInstance(project).recentTasks
        // GradleSettings.getInstance(project).linkedProjectsSettings.forEach { }
        // val isGradleAvailable = project.allModules().any { ExternalSystemApiUtil.isExternalSystemAwareModule(GradleConstants.SYSTEM_ID, it) }

        // return super.isApplicable(project)
        return false
    }
}