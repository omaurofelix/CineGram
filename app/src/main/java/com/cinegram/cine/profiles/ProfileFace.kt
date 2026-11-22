package com.cinegram.cine.profiles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

val AvatarGradients = listOf(
    Color(0xFF4FA8F5) to Color(0xFF0B5FC0),
    Color(0xFF3E8F9E) to Color(0xFF0B2E38),
    Color(0xFFF0413C) to Color(0xFFA8121E),
    Color(0xFFF5C443) to Color(0xFFC08A0B),
    Color(0xFF4BC98A) to Color(0xFF12805A),
    Color(0xFFA06BF5) to Color(0xFF4A1EA8),
)

@Composable
fun ProfileFace(avatar: Int, size: Dp, corner: Dp, modifier: Modifier = Modifier) {
    val face = AvatarGradients[avatar % AvatarGradients.size]

    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(corner))
            .background(Brush.verticalGradient(listOf(face.first, face.second))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val eye = w * 0.055f
            drawCircle(Color.White, eye, Offset(w * 0.30f, h * 0.30f))
            drawCircle(Color.White, eye, Offset(w * 0.70f, h * 0.30f))
            val r = w * 0.23f
            drawArc(
                color = Color.White,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.5f - r, h * 0.40f - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = w * 0.022f, cap = StrokeCap.Round),
            )
        }
    }
}
