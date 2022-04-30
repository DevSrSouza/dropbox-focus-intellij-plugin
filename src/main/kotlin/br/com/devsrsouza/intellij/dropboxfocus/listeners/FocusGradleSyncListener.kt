package br.com.devsrsouza.intellij.dropboxfocus.listeners

import br.com.devsrsouza.intellij.dropboxfocus.services.FocusGradleSettingsReader
import br.com.devsrsouza.intellij.dropboxfocus.services.FocusService
import com.android.tools.idea.gradle.project.sync.GradleSyncListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

// This is required because apparently, with the addiction of syncStarted, in the newer version
// the bytecode got incompatible.
fun createFocusGradleSyncListener(): GradleSyncListener {
    return Proxy.newProxyInstance(
        ThemeChangeListener::class.java.classLoader,
        arrayOf(GradleSyncListener::class.java),
        InvocationHandler { proxy, method, args ->
            fun project() = args.get(0) as Project
            fun finishOperation() {
                project().service<FocusService>()._focusOperationState.value = false
            }
            when (method.name) {
                "syncSucceeded" -> {
                    finishOperation()
                    project().service<FocusGradleSettingsReader>().reloadProjectFocusSettings()
                }
                "syncFailed" -> {
                    finishOperation()
                }
                "syncSkipped" -> {
                    finishOperation()
                }
                "syncStarted" -> {
                    // Available since 2021.1 (if I recall, so we use it)
                    project().service<FocusService>()._focusOperationState.value = true
                }
            }
        }
    ) as GradleSyncListener
}