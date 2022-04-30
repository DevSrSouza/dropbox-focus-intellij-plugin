package br.com.devsrsouza.intellij.dropboxfocus.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.intellij.ui.scale.JBUIScale
import io.kanro.compose.jetbrains.JBTypography
import io.kanro.compose.jetbrains.SwingColor

private fun TextStyle.jbScale(): TextStyle = copy(
    fontSize = (JBUIScale.scale(fontSize.value)).sp
)

@Composable
fun intelliJTypography(): JBTypography {
    return remember(SwingColor.themeChangeState) {
        JBTypography().run {
            copy(
                h0 = h0.jbScale(),
                h1 = h1.jbScale(),
                h2 = h2.jbScale(),
                h2Bold = h2Bold.jbScale(),
                h3 = h3.jbScale(),
                h3Bold = h3Bold.jbScale(),
                default = default.jbScale(),
                defaultBold = defaultBold.jbScale(),
                defaultUnderlined = defaultUnderlined.jbScale(),
                paragraph = paragraph.jbScale(),
                medium = medium.jbScale(),
                mediumBold = mediumBold.jbScale(),
                small = small.jbScale(),
                smallUnderlined = smallUnderlined.jbScale(),
            )
        }
    }
}