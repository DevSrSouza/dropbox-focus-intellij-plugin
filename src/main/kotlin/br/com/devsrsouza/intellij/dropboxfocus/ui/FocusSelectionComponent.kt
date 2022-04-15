package br.com.devsrsouza.intellij.dropboxfocus.ui

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettings
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusModule
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import br.com.devsrsouza.intellij.dropboxfocus.ui.util.onTextChange
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.openapi.ui.panel.ComponentPanelBuilder
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class FocusSelectionComponent(
    private val focusSettings: FocusGradleSettings?,
    private val focusService: FocusService,
    private val onSelectModule: () -> Unit = {},
) {
    private val focusModulesListPanel by lazy { JPanel(VerticalFlowLayout()) }
    private var currentFocusJLabel: JLabel? = null
    private var currentSearchValue: String = ""
    private var currentFocusRow: Row? = null

    fun render(builder: LayoutBuilder) = with(builder) {
        currentFocusRow = row {
            cell {
                comment("Current focus")
                label(currentFocus(focusSettings)).also {
                    currentFocusJLabel = it.component
                }
            }
        }.apply {
            visible = isCurrentFocusVisible(focusSettings)
        }

        row { comment("Search") }
        row {
            component(
                JBTextField().apply {
                    onTextChange {
                        currentSearchValue = text
                        updateModulesList(focusSettings)
                    }
                }
            ).focused()
        }

        row { comment("Choose a module to Focus") }

        row {
            component(JBScrollPane(focusModulesListPanel)).constraints(CCFlags.grow)
        }
    }.also {
        updateModulesList(focusSettings)
    }

    fun update(focusSettings: FocusGradleSettings?) {
        updateModulesList(focusSettings)
        currentFocusRow?.visible = isCurrentFocusVisible(focusSettings)
        currentFocusJLabel?.text = currentFocus(focusSettings)
    }

    private fun isCurrentFocusVisible(focusSettings: FocusGradleSettings?): Boolean =
        focusSettings?.currentFocusModulePath != null
    private fun currentFocus(focusSettings: FocusGradleSettings?) =
        focusSettings?.currentFocusModulePath ?: "None"

    private fun updateModulesList(focusGradleSettings: FocusGradleSettings?) {
        focusModulesListPanel.apply {
            removeAll()
            if (focusGradleSettings == null) {
                add(
                    ComponentPanelBuilder.createCommentComponent(
                        "No focus module available",
                        true,
                        1,
                        true
                    )
                )
            } else {
                for (module in focusGradleSettings.modulesFilter()) {
                    add(
                        JButton(module.gradleModulePath).apply {
                            addActionListener {
                                focusGradleSettings.selectModule(module)
                            }
                        }
                    )
                }
            }
            revalidate()
            repaint()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun FocusGradleSettings.modulesFilter() = allModules.filter {
        currentSearchValue.isBlank() ||
            currentSearchValue.lowercase() in it.gradleModulePath.lowercase()
    }

    private fun FocusGradleSettings.selectModule(module: FocusModule) {
        onSelectModule()
        focusService.focusOn(this, module.gradleModulePath)
    }
}