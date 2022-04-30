package br.com.devsrsouza.intellij.dropboxfocus.ui

import androidx.compose.runtime.Composable
import io.kanro.compose.jetbrains.JBThemeFromIntelliJ

@Composable
fun Theme(content: @Composable () -> Unit) {
    JBThemeFromIntelliJ(
        typography = intelliJTypography()
    ) {
        content()
    }
}