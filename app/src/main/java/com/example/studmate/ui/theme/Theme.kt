package com.example.studmate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.studmate.domain.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = AcademicIndigo,
    onPrimary = IndigoOnPrimary,
    primaryContainer = SoftPeriwinkle,
    onPrimaryContainer = OnSoftPeriwinkle,
    secondary = ScholarTeal,
    onSecondary = TealOnSecondary,
    secondaryContainer = MintMist,
    onSecondaryContainer = OnMintMist,
    tertiary = WarmAmber,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = AlabasterSlate,
    surface = CrispSurface,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurface = InkSlate,
    onSurfaceVariant = MutedSlate,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
)

private val DarkColorScheme = darkColorScheme(
    primary = AcademicIndigoDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = SoftPeriwinkleDark,
    onPrimaryContainer = OnSoftPeriwinkleDark,
    secondary = ScholarTealDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = MintMistDark,
    onSecondaryContainer = OnMintMistDark,
    tertiary = WarmAmberDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF334155),
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
)

@Composable
fun StudMateTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
