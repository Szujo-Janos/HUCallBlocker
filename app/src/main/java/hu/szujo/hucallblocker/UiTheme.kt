package hu.szujo.hucallblocker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal enum class ThemeMode(val title: String) {
    SYSTEM("Rendszer"), LIGHT("Világos"), DARK("Sötét")
}

internal object ShieldColors {
    val navy = Color(0xFF091F2A)
    val darkGreen = Color(0xFF123A35)
    val mint = Color(0xFF31DAB3)
    val softMint = Color(0xFFDDF8EF)
    val red = Color(0xFFCB4458)
    val amber = Color(0xFFF0B44E)
    val cloud = Color(0xFFF4F8F7)
}

private val lightColors = lightColorScheme(
    primary = Color(0xFF007E67),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F6EB),
    onPrimaryContainer = Color(0xFF004737),
    secondary = Color(0xFF38685D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEFEA),
    onSecondaryContainer = Color(0xFF24483F),
    background = ShieldColors.cloud,
    onBackground = ShieldColors.navy,
    surface = Color.White,
    onSurface = ShieldColors.navy,
    surfaceVariant = Color(0xFFEAF1EF),
    onSurfaceVariant = Color(0xFF506A67),
    outline = Color(0xFFB8CFC9),
    error = ShieldColors.red
)

private val darkColors = darkColorScheme(
    primary = ShieldColors.mint,
    onPrimary = Color(0xFF05382E),
    primaryContainer = Color(0xFF125B4C),
    onPrimaryContainer = Color(0xFFC8FFF0),
    secondary = Color(0xFFA6D8C9),
    onSecondary = Color(0xFF183C32),
    secondaryContainer = Color(0xFF2B4C45),
    onSecondaryContainer = Color(0xFFCBF4E7),
    background = Color(0xFF0B161C),
    onBackground = Color(0xFFF1F9F6),
    surface = Color(0xFF14232B),
    onSurface = Color(0xFFF1F9F6),
    surfaceVariant = Color(0xFF20343A),
    onSurfaceVariant = Color(0xFFA8C4BE),
    outline = Color(0xFF4F6B68),
    error = Color(0xFFFF8894)
)

private val shieldTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold),
    headlineMedium = TextStyle(fontSize = 25.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium)
)

@Composable
internal fun ShieldTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) darkColors else lightColors,
        typography = shieldTypography,
        content = content
    )
}
