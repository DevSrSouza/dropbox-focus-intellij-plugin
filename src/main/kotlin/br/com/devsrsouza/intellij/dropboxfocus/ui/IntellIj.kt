package br.com.devsrsouza.intellij.dropboxfocus.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.UITheme
import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.ide.ui.laf.darcula.DarculaLookAndFeelInfo
import com.intellij.ui.scale.JBUIScale
import io.kanro.compose.jetbrains.JBTypography
import io.kanro.compose.jetbrains.SwingColor
import io.kanro.compose.jetbrains.color.CheckBoxColors
import io.kanro.compose.jetbrains.color.darkCheckBoxColors
import io.kanro.compose.jetbrains.color.lightCheckBoxColors
import javax.swing.UIManager

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

fun CheckBoxColors.updateIntelliJColorsFixed() {
    val fromTheme = when (val laf = LafManager.getInstance().currentLookAndFeel) {
        is DarculaLookAndFeelInfo -> darkCheckBoxColors()
        is UIThemeBasedLookAndFeelInfo -> {
            if (laf.theme.name == "IntelliJ Light") {
                lightCheckBoxColors()
            } else {
                val fallback = if (laf.theme.isDark) darkCheckBoxColors() else lightCheckBoxColors()
                val theme = laf.theme
                with(fallback) {
                    CheckBoxColors(
                        bg = theme.color("Checkbox.Background.Default")
                            ?: bg,
                        bgSelected = theme.color("Checkbox.Background.Selected")
                            ?: theme.color("Checkbox.Background.Default")
                            ?: bgSelected,
                        bgDisabled = theme.color("Checkbox.Background.Default")
                            ?: bgDisabled,
                        border = theme.color("Checkbox.Border.Default")
                            ?: border, // dont know
                        borderSelected = theme.color("Checkbox.Border.Selected")
                            ?: theme.color("Checkbox.Focus.Thin.Selected")
                            ?: borderSelected, // dont know
                        borderFocused = theme.color("Checkbox.Focus.Thin.Selected")
                            ?: theme.color("Checkbox.Border.Default")
                            ?: borderFocused,
                        borderDisabled = theme.color("Checkbox.Border.Disabled")
                            ?: borderDisabled
                    )
                }
            }
        }
        else -> null
    }

    bg = fromTheme?.bg ?: bg
    bgSelected = fromTheme?.bgSelected ?: bgSelected
    bgDisabled = fromTheme?.bgDisabled ?: bgDisabled
    border = fromTheme?.border ?: border
    borderSelected = fromTheme?.borderSelected ?: borderSelected
    borderFocused = fromTheme?.borderFocused ?: borderFocused
    borderDisabled = fromTheme?.borderDisabled ?: borderDisabled
}

private val iconsField by lazy {
    UITheme::class.java.getDeclaredField("icons")
        .apply { isAccessible = true }
}

internal val UITheme.icons: Map<String, Any>? get() =
    iconsField.get(this) as? Map<String, Any>?

internal val UITheme.colorPalette: Map<String, String>? get() =
    ((icons?.get("ColorPalette") as? Map<String, Any>?)?.filterValues { it is String })
        as? Map<String, String>?

private fun TextStyle.jbScale(): TextStyle = copy(
    fontSize = (JBUIScale.scale(fontSize.value)).sp
)

private fun UITheme.color(name: String): Color? =
    if (name.endsWith(".Dark")) {
        colorPalette?.get(name)?.hexToColor
    } else {
        colorPalette?.get(name)?.hexToColor ?: color("$name.Dark")
    }

private val String.hexToColor: Color? get() = runCatching {
    java.awt.Color.decode(
        this.toUpperCase()
            .replaceBeforeLast("#", "")
    ).asComposeColor
}.getOrNull()

private val java.awt.Color.asComposeColor: Color get() = Color(red, green, blue, alpha)

private fun getSwingColor(vararg key: String): Color? {
    return key.firstNotNullOfOrNull { UIManager.getColor(it) }?.asComposeColor
}