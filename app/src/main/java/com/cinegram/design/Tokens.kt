package com.cinegram.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Os valores da especificação visual do CineGram, num lugar só.
 * Preto profundo, um acento dessaturado, movimento curto e nada de enfeite.
 */
object Cine {
    val Bg = Color(0xFF0A0A0B)
    val BgElevated = Color(0xFF141416)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF8E8E93)
    val Accent = Color(0xFFC4463F)
    val FocusLine = Color(0xFFFFFFFF)

    const val FocusScale = 1.08f
    val FocusShadow = 24.dp
    const val FadeMs = 240
    const val HeroFadeMs = 400

    val PosterWidth = 180.dp
    const val PosterRatio = 2f / 3f
    val RowGap = 16.dp
    val ScreenMargin = 56.dp
}
