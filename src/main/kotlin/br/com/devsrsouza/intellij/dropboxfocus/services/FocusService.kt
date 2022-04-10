package br.com.devsrsouza.intellij.dropboxfocus.services

import com.android.tools.idea.gradle.project.sync.GradleSyncInvoker
import com.android.tools.idea.gradle.project.sync.GradleSyncListener
import com.android.tools.idea.util.toIoFile
import com.google.wireless.android.sdk.stats.GradleSyncStats
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File

@Service
class FocusService(private val project: Project) {

    fun focusOn(focusSettings: FocusSettings, focusModulePath: String) {
        clearFocus(focusSettings, requireSync = false)

        runGradleTask("$focusModulePath:focus", onSuccess = {
            syncGradle()
        })
    }

    // RequireSync when used on Actions or on ClearFocus on the project startup clear focus selection
    fun clearFocus(focusSettings: FocusSettings, requireSync: Boolean) {
        val dir = project.guessProjectDir()?.toIoFile() ?: return

        File(dir, focusSettings.focusFileName).takeIf(File::exists)?.delete()

        if (requireSync) {
            syncGradle()
        }
    }

    private fun runGradleTask(
        task: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {},
    ) {
        val settings = ExternalSystemTaskExecutionSettings().apply {
            externalProjectPath = project.basePath
            taskNames = listOf(task)
            vmOptions = ""
            externalSystemIdString = GradleConstants.SYSTEM_ID.id
        }

        ExternalSystemUtil.runTask(
            settings,
            DefaultRunExecutor.EXECUTOR_ID,
            project,
            GradleConstants.SYSTEM_ID,
            object : TaskCallback {
                override fun onSuccess() {
                    onSuccess()
                }

                override fun onFailure() {
                    onFailure()
                }
            },
            ProgressExecutionMode.NO_PROGRESS_ASYNC,
            true,
        )
    }

    private fun syncGradle() {
        // TODO: For Android Studio is required another solution, this one does not work apparently
        GradleSyncInvoker.getInstance().requestProjectSync(
            project,
            GradleSyncStats.Trigger.TRIGGER_PROJECT_MODIFIED,
            object : GradleSyncListener {
            }
        )
    }
}
