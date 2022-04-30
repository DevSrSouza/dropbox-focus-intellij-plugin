package br.com.devsrsouza.intellij.dropboxfocus.listeners

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import io.kanro.compose.jetbrains.SwingColor

class ThemeChangeListener : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        SwingColor.onThemeChange()
    }
}