package br.com.devsrsouza.intellij.dropboxfocus.services

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import br.com.devsrsouza.intellij.dropboxfocus.ui.Theme
import com.intellij.openapi.components.service
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import io.kanro.compose.jetbrains.control.CheckBox
import io.kanro.compose.jetbrains.control.JPanel
import io.kanro.compose.jetbrains.control.Text
import javax.swing.JComponent

class FocusConfigurable(private val project: Project) : SearchableConfigurable {

    private val settings = project.service<FocusSettings>()

    private val settingsPanel by lazy {
        ComposePanel().apply {
            setContent {
                Theme {
                    JPanel(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.padding(all = 16.dp)
                        ) {
                            Row {
                                var checkState by remember {
                                    mutableStateOf(settings.shouldShowStartupDialog)
                                }
                                CheckBox(
                                    checked = checkState,
                                    onCheckedChange = {
                                        settings.shouldShowStartupDialog = !checkState
                                        checkState = !checkState
                                    }
                                )
                                Text(
                                    text = "Show Focus startup dialog for this project",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun createComponent(): JComponent = settingsPanel
    override fun isModified(): Boolean = false

    override fun apply() = Unit

    override fun getDisplayName(): String = "Dropbox Focus"

    override fun getId(): String = ID

    companion object {
        const val ID = "br.com.devsrsouza.intellij.dropboxfocus.configurable"
    }
}