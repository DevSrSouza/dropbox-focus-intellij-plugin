package br.com.devsrsouza.intellij.dropboxfocus.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.kanro.compose.jetbrains.JBThemeFromIntelliJ
import io.kanro.compose.jetbrains.SwingColor
import io.kanro.compose.jetbrains.color.LocalCheckBoxColors

@Composable
fun Theme(content: @Composable () -> Unit) {
    JBThemeFromIntelliJ(
        typography = intelliJTypography()
    ) {
        val currentCheckboxColors = LocalCheckBoxColors.current
        remember(SwingColor.themeChangeState) {
            currentCheckboxColors.updateIntelliJColorsFixed()
        }
        content()
    }
}