package br.com.devsrsouza.intellij.dropboxfocus.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import org.jetbrains.kotlin.idea.util.application.invokeLater

@Service
class FocusToolWindowService(private val project: Project) : Disposable {
    private val job = Job()

    fun init() {
        ToolWindowManager.getInstance(project).invokeLater {
            GlobalScope.launch(Dispatchers.Swing + job) {
                project.service<FocusGradleSettingsReader>().focusGradleSettings.collect {
                    val toolWindow = ToolWindowManager.getInstance(project)
                        .getToolWindow("Focus")

                    toolWindow?.isAvailable = it != null
                }
            }
        }
    }
    override fun dispose() {
        job.cancel()
    }
}