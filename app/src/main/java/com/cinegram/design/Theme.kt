package com.cinegram.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * O tema do CineGram: só o esquema escuro, tipografia leve e espaçada.
 * Nada aqui deve competir com a arte do conteúdo.
 */
private val CineScheme = darkColorScheme(
    background = Cine.Bg,
    surface = Cine.BgElevated,
    onBackground = Cine.TextPrimary,
    onSurface = Cine.TextPrimary,
    primary = Cine.Accent,
    onPrimary = Cine.TextPrimary,
)

private val BaseType = Typography()

private fun TextStyle.sf() = copy(fontFamily = SfPro)

private val CineTypography = Typography(
    displayLarge = BaseType.displayLarge.sf(),
    displayMedium = BaseType.displayMedium.sf(),
    displaySmall = BaseType.displaySmall.sf().copy(
        fontSize = 44.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-0.5).sp,
    ),
    headlineLarge = BaseType.headlineLarge.sf(),
    headlineMedium = BaseType.headlineMedium.sf(),
    headlineSmall = BaseType.headlineSmall.sf().copy(fontSize = 22.sp),
    titleLarge = BaseType.titleLarge.sf(),
    titleMedium = BaseType.titleMedium.sf(),
    titleSmall = BaseType.titleSmall.sf(),
    bodyLarge = BaseType.bodyLarge.sf(),
    bodyMedium = BaseType.bodyMedium.sf().copy(fontSize = 15.sp),
    bodySmall = BaseType.bodySmall.sf(),
    labelLarge = BaseType.labelLarge.sf(),
    labelMedium = BaseType.labelMedium.sf(),
    labelSmall = BaseType.labelSmall.sf().copy(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp,
    ),
)

@Composable
fun CineTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CineScheme, typography = CineTypography, content = content)
}
