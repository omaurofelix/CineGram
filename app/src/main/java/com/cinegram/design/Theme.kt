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

private val CineTypography = Typography(
    displaySmall = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Light, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.6.sp),
)

@Composable
fun CineTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CineScheme, typography = CineTypography, content = content)
}
