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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File

@Service
class FocusService(private val project: Project) {
    internal val _focusOperationState = MutableStateFlow(false)
    val focusOperationState: StateFlow<Boolean> = _focusOperationState

    fun focusOn(focusSettings: FocusGradleSettings, focusModulePath: String) {
        _focusOperationState.value = true
        try {
            clearFocus(focusSettings, requireSync = false)

            runGradleTask("$focusModulePath:focus", onSuccess = {
                syncGradle()
            })
        } catch (e: Exception) {
            _focusOperationState.value = false
        }
    }

    // RequireSync when used on Actions or on ClearFocus on the project startup clear focus selection
    fun clearFocus(focusSettings: FocusGradleSettings, requireSync: Boolean) {
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
            false,
        )
    }

    private val gradleProjectImporterInstance by lazy {
        GradleSyncInvoker::class.java.getDeclaredMethod("getInstance").invoke(null)
    }

    private val requestProjectSyncMethod by lazy {
        GradleSyncInvoker::class.java.getDeclaredMethod(
            "requestProjectSync",
            Project::class.java,
            GradleSyncStats.Trigger::class.java,
            GradleSyncListener::class.java,
        )
    }

    private val requestProjectSyncMethodWithRequest by lazy {
        GradleSyncInvoker::class.java.getDeclaredMethod(
            "requestProjectSync",
            Project::class.java,
            GradleSyncInvoker.Request::class.java,
            GradleSyncListener::class.java,
        )
    }

    fun syncGradle() {
        _focusOperationState.value = true

        runCatching {
            // Reflection required to fix bytecode incompatibility with AS 2021.3.1 Canary 7
            requestProjectSyncMethod.invoke(
                gradleProjectImporterInstance,
                project,
                GradleSyncStats.Trigger.TRIGGER_PROJECT_MODIFIED,
                null
            )
        }.onFailure {
            // AS 2022.1 Canary 1 does not have a function with [Trigger] type
            requestProjectSyncMethodWithRequest.invoke(
                gradleProjectImporterInstance,
                project,
                GradleSyncInvoker.Request(GradleSyncStats.Trigger.TRIGGER_PROJECT_MODIFIED),
                null,
            )
        }
    }
}