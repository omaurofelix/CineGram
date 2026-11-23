package com.cinegram.design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Valores da especificação visual do CineGram.
 * O foco é dado só pelo tamanho e pela sombra nos vizinhos, sem brilho de fundo.
 */
object Cine {
    val Bg = Color(0xFF0A0A0A)
    val BgElevated = Color(0xFF141417)
    val Surface = Color(0xFF17171A)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF9A9AA2)
    val Accent = Color(0xFFC4463F)
    val FocusRing = Color(0xFFF5F5F7)
    val Pill = Color(0x2EFFFFFF)
    val Badge = Color(0xD9121214)

    /** Sombra aplicada aos vizinhos, por distância do centro. */
    const val ShadowNear = 0.45f
    const val ShadowFar = 0.74f

    /** Encolhimento dos vizinhos, por distância do centro. */
    const val ScaleNear = 0.82f
    const val ScaleFar = 0.66f

    const val SlideMs = 280
    const val FadeMs = 200

    val FocusRingWidth = 3.dp
    val FocusScale = 1.04f

    val ShapeRadius = 14.dp
    val ScreenMargin = 56.dp
    val RowGap = 14.dp

    /** Capa da faixa. O central usa a largura cheia, os vizinhos encolhem. */
    val CardWidth = 280.dp
    const val CardRatio = 2f / 3f
    val CardRowHeight = 448.dp

    /** Sobras de telas antigas, mantidas para os componentes já existentes. */
    val PosterWidth = 150.dp
    const val PosterRatio = 2f / 3f
    val LandscapeWidth = 220.dp
    const val LandscapeRatio = 16f / 9f
    val RowHeight = 200.dp
}
