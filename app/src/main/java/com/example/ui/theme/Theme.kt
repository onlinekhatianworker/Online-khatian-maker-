package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = CivicEmerald,
    onPrimary = Color.White,
    primaryContainer = CivicEmeraldContainer,
    onPrimaryContainer = OnCivicEmeraldContainer,
    secondary = HeritageBrass,
    onSecondary = Color.White,
    secondaryContainer = HeritageBrassContainer,
    onSecondaryContainer = OnHeritageBrassContainer,
    tertiary = DeepCivicSlate,
    onTertiary = Color.White,
    tertiaryContainer = DeepCivicSlateContainer,
    onTertiaryContainer = OnDeepCivicSlateContainer,
    background = WarmIvoryBackground,
    onBackground = OnWarmInk,
    surface = CrispPaperSurface,
    onSurface = OnWarmInk,
    surfaceVariant = SoftSageSurfaceVariant,
    onSurfaceVariant = OnMutedInk,
    outline = OutlineSage,
    outlineVariant = OutlineSageVariant,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = MintEmeraldDark,
    onPrimary = OnMintEmeraldDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = CivicEmeraldContainer,
    secondary = AmberGoldDark,
    onSecondary = OnHeritageBrassContainer,
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = HeritageBrassContainer,
    tertiary = SlateBlueDark,
    onTertiary = OnDeepCivicSlateContainer,
    tertiaryContainer = SlateContainerDark,
    onTertiaryContainer = DeepCivicSlateContainer,
    background = DarkCharcoalBackground,
    onBackground = OnDarkInk,
    surface = DarkSlateSurface,
    onSurface = OnDarkInk,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkMutedInk,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun OnlineKhatianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
