package br.com.devsrsouza.intellij.dropboxfocus.ui.util

import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal fun JTextField.onTextChange(callback: (String) -> Unit) {
    document.addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            callback(text)
        }

        override fun removeUpdate(e: DocumentEvent?) {
            callback(text)
        }

        override fun changedUpdate(e: DocumentEvent?) {
            callback(text)
        }
    })
}