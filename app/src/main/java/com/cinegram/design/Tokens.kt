package com.cinegram.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Valores da especificação visual do CineGram, no padrão do JetStream:
 * contorno de foco de 3dp, raio sutil e escala tipográfica clara.
 */
object Cine {
    val Bg = Color(0xFF0B0B0D)
    val BgElevated = Color(0xFF16161A)
    val Surface = Color(0xFF1E1E24)
    val TextPrimary = Color(0xFFF4F4F6)
    val TextSecondary = Color(0xFF9A9AA2)
    val Accent = Color(0xFFC4463F)
    val FocusRing = Color(0xFFF4F4F6)

    val FocusRingWidth = 3.dp
    val FocusScale = 1.04f
    const val FadeMs = 200

    val ShapeRadius = 8.dp
    val ScreenMargin = 56.dp
    val RowGap = 16.dp

    val PosterWidth = 150.dp
    const val PosterRatio = 2f / 3f
    val LandscapeWidth = 220.dp
    const val LandscapeRatio = 16f / 9f
    val RowHeight = 200.dp
}
