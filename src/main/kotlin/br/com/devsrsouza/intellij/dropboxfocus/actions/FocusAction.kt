package br.com.devsrsouza.intellij.dropboxfocus.actions

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettingsReader
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.guessProjectDir

val logger by lazy { IdeaLogger.getFactory().getLoggerInstance("Focus") }

class FocusAction : AnAction() {
    private companion object {
        const val FOCUS_ON_MODULE_TEXT = "Focus on Module"
        const val CLEAR_FOCUS_TEXT = "Clear Focus"
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val focusSettings = project?.service<FocusSettingsReader>()?.getProjectFocusSettings() ?: return
        val clickedFolderPath = event.getData(CommonDataKeys.VIRTUAL_FILE)?.toNioPath() ?: return

        val rootDir = project.guessProjectDir()?.toNioPath()

        when {
            rootDir == clickedFolderPath -> {
                project.service<FocusService>().clearFocus(focusSettings, requireSync = true)
            }
            else -> {
                val focusModule = focusSettings.allModules.find { it.moduleDirPath == clickedFolderPath } ?: return
                project.service<FocusService>().focusOn(focusSettings, focusModule.gradleModulePath)
            }
        }
    }

    override fun update(event: AnActionEvent) {
        fun hide() {
            event.presentation.isVisible = false
        }

        val clickedFolderPath = event.getData(CommonDataKeys.VIRTUAL_FILE)?.toNioPath() ?: run {
            hide()
            return
        }

        val project = event.project
        val focusSettings = project?.service<FocusSettingsReader>()?.getProjectFocusSettings() ?: run {
            hide()
            return
        }
        val rootDir = project.guessProjectDir()?.toNioPath()

        when {
            rootDir == clickedFolderPath -> {
                event.presentation.text = CLEAR_FOCUS_TEXT
                event.presentation.isEnabled = focusSettings.currentFocus != null
            }
            focusSettings.allModules.any { it.moduleDirPath == clickedFolderPath } -> {
                event.presentation.text = FOCUS_ON_MODULE_TEXT
            }
            else -> hide()
        }
    }
}
