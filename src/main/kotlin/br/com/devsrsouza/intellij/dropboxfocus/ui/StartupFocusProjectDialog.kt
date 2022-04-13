package br.com.devsrsouza.intellij.dropboxfocus.ui

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusModule
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusSettings
import br.com.devsrsouza.intellij.dropboxfocus.ui.util.onTextChange
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.panel
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class StartupFocusProjectDialog(
    private val project: Project,
    private val focusSettings: FocusGradleSettings,
    private val focusService: FocusService,
) : DialogWrapper(true) {

    private val focusModulesListPanel by lazy { JPanel(VerticalFlowLayout()) }
    private var currentSearchValue: String = ""
    init {
        title = "Focus"
        setCancelButtonText("Clear focus")
        setOKButtonText("Don't change")
        doNotOpenOnStartupCheckBox()
        init()
    }

    override fun createCenterPanel(): JComponent? = swing()

    private fun swing() = panel {
        if (focusSettings.currentFocusModulePath != null) {
            row {
                cell {
                    comment("Current focus")
                    label("${focusSettings.currentFocusModulePath}")
                }
            }
        }

        row { comment("Search") }
        row {
            component(
                JBTextField().apply {
                    onTextChange {
                        currentSearchValue = text
                        updateModulesList()
                    }
                }
            ).focused()
        }

        row { comment("Choose a module to Focus") }

        row {
            component(JBScrollPane(focusModulesListPanel)).constraints(CCFlags.grow)
        }
    }.also {
        updateModulesList()
    }

    private fun updateModulesList() {
        focusModulesListPanel.apply {
            removeAll()
            for (module in modulesFilter()) {
                add(
                    JButton(module.gradleModulePath).apply {
                        addActionListener {
                            selectModule(module)
                        }
                    }
                )
            }
            revalidate()
            repaint()
        }
    }

    override fun doCancelAction() {
        super.doCancelAction()
        if (cancelAction.isEnabled) {
            focusService.clearFocus(focusSettings, requireSync = true)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun modulesFilter() = focusSettings.allModules.filter {
        currentSearchValue.isBlank() ||
            currentSearchValue.lowercase() in it.gradleModulePath.lowercase()
    }

    private fun selectModule(module: FocusModule) {
        close(0)
        focusService.focusOn(focusSettings, module.gradleModulePath)
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