package app.petrapsheet.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Paper = Color(0xFFF3EDDD)
val PaperDark = Color(0xFFE6DDC6)
val InkBlack = Color(0xFF1E1C1A)
val InkFaded = Color(0xFF6B655C)
val StampRed = Color(0xFFB0281F)

private val Colors = lightColorScheme(
    primary = InkBlack,
    onPrimary = Paper,
    secondary = StampRed,
    onSecondary = Paper,
    background = Paper,
    onBackground = InkBlack,
    surface = Paper,
    onSurface = InkBlack,
    surfaceVariant = PaperDark,
    onSurfaceVariant = InkFaded,
    surfaceContainerHighest = PaperDark,
    secondaryContainer = PaperDark,
    onSecondaryContainer = InkBlack,
    outline = InkFaded,
    outlineVariant = Color(0xFFBFB49C),
)

@Composable
fun PetRapSheetTheme(content: @Composable () -> Unit) {
    val assets = LocalContext.current.assets
    val typewriter = remember(assets) {
        FontFamily(
            Font("fonts/CourierPrime-Regular.ttf", assets, FontWeight.Normal),
            Font("fonts/CourierPrime-Bold.ttf", assets, FontWeight.Bold),
        )
    }
    val typography = remember(typewriter) { typewriterTypography(typewriter) }
    MaterialTheme(colorScheme = Colors, typography = typography, content = content)
}

private fun typewriterTypography(family: FontFamily): Typography {
    val base = Typography()
    fun TextStyle.typed() = copy(fontFamily = family)
    return Typography(
        displayLarge = base.displayLarge.typed(),
        displayMedium = base.displayMedium.typed(),
        displaySmall = base.displaySmall.typed(),
        headlineLarge = base.headlineLarge.typed().copy(fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.typed().copy(fontWeight = FontWeight.Bold),
        headlineSmall = base.headlineSmall.typed().copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.typed().copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.typed().copy(fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.typed().copy(fontWeight = FontWeight.Bold),
        bodyLarge = base.bodyLarge.typed(),
        bodyMedium = base.bodyMedium.typed(),
        bodySmall = base.bodySmall.typed(),
        labelLarge = base.labelLarge.typed().copy(fontWeight = FontWeight.Bold),
        labelMedium = base.labelMedium.typed(),
        labelSmall = base.labelSmall.typed(),
    )
}
