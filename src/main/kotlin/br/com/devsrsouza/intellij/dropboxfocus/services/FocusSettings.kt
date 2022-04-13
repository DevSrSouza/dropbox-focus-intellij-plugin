package br.com.devsrsouza.intellij.dropboxfocus.services

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@State(
    name = "DropboxFocusSettings",
    storages = [Storage("dropbox-focus.xml")]
)
class FocusSettings(
    internal val project: Project
) : SimplePersistentStateComponent<FocusSettingsState>(FocusSettingsState()) {
    var shouldShowStartupDialog
        get() = state.shouldShowStartupDialog
        set(value) { state.shouldShowStartupDialog = value }

    override fun noStateLoaded() {
        super.noStateLoaded()
        loadState(FocusSettingsState())
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): FocusGradleSettings = project.service()
    }
}

class FocusSettingsState : BaseState() {
    var shouldShowStartupDialog by property(true)
}