package br.com.devsrsouza.intellij.dropboxfocus.ui.dialog

import androidx.compose.ui.awt.ComposePanel
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettings
import br.com.devsrsouza.intellij.dropboxfocus.ui.FocusSelection
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class StartupFocusProjectDialog(
    private val project: Project,
    private val focusSettings: FocusGradleSettings,
    private val focusService: FocusService,
) : DialogWrapper(true) {

    init {
        title = "Focus"
        setCancelButtonText("Clear focus")
        setOKButtonText("Don't change")
        doNotOpenOnStartupCheckBox()
        init()
    }

    override fun createCenterPanel(): JComponent? = swing()

    private fun swing() =
        ComposePanel().apply {
            size = Dimension(300, 300)
            setContent {
                FocusSelection(
                    currentFocusGradleSettingsState = MutableStateFlow(focusSettings),
                    isLoadingState = MutableStateFlow(false),
                    syncGradle = {},
                    selectModuleToFocus = { settings, module ->
                        focusService.focusOn(settings, module.gradleModulePath)
                        close(0)
                    },
                    clearFocus = {},
                    withClearFocusButton = false,
                )
            }
        }

    override fun doCancelAction() {
        super.doCancelAction()
        if (cancelAction.isEnabled) {
            focusService.clearFocus(focusSettings, requireSync = true)
        }
    }

    override fun createActions(): Array<Action> {
        if (focusSettings.currentFocusModulePath != null) {
            return arrayOf(okAction, cancelAction)
        } else {
            return arrayOf(okAction)
        }
    }

    fun doNotOpenOnStartupCheckBox() {
        setDoNotAskOption(object : DoNotAskOption {
            override fun isToBeShown(): Boolean = project.service<FocusSettings>().shouldShowStartupDialog

            override fun setToBeShown(toBeShown: Boolean, exitCode: Int) {
                project.service<FocusSettings>().shouldShowStartupDialog = toBeShown
            }

            override fun canBeHidden(): Boolean = true

            override fun shouldSaveOptionsOnCancel(): Boolean = true

            override fun getDoNotShowMessage(): String = "Do not show for this project"
        })
    }
}