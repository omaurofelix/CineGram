package com.cinegram.cine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinegram.design.Cine

private data class FakeShow(val title: String, val year: String, val tone: Color)

private val FakeRow = listOf(
    FakeShow("Tom Tem Dez Anos", "2021", Color(0xFF2E3A45)),
    FakeShow("Outra Série", "2023", Color(0xFF3A2E33)),
    FakeShow("Uma Terceira", "2020", Color(0xFF2B3A2E)),
    FakeShow("Mais Uma", "2022", Color(0xFF332F45)),
    FakeShow("E Outra", "2019", Color(0xFF45382B)),
    FakeShow("Última", "2024", Color(0xFF2A2A2A)),
)

@Composable
fun HomeScreen() {
    val firstFocus = remember { FocusRequester() }

    Box(Modifier.fillMaxSize().background(Cine.Bg)) {
        // Fundo do destaque: degradê, até termos arte de verdade.
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B242B), Cine.Bg, Cine.Bg),
                ),
            ),
        )

        Column(
            Modifier.fillMaxSize().padding(horizontal = Cine.ScreenMargin),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                "CineGram",
                style = MaterialTheme.typography.labelSmall,
                color = Cine.TextSecondary,
            )

            Spacer(Modifier.weight(1f))

            Text(
                "Tom Tem Dez Anos",
                style = MaterialTheme.typography.displaySmall,
                color = Cine.TextPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "2021  ·  2 temporadas  ·  TV-14",
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.TextSecondary,
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PrimaryButton("▶  Assistir") {}
                GhostButton("Mais informações") {}
            }

            Spacer(Modifier.height(36.dp))

            Text(
                "CONTINUAR ASSISTINDO",
                style = MaterialTheme.typography.labelSmall,
                color = Cine.TextSecondary,
            )

            Spacer(Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                items(FakeRow) { show ->
                    PosterCard(show)
                }
            }
        }
    }

    LaunchedEffect(Unit) { firstFocus.requestFocus() }
}

@Composable
private fun PosterCard(show: FakeShow) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) Cine.FocusScale else 1f)
    val focusRequester = remember { FocusRequester() }

    Column(Modifier.width(Cine.PosterWidth).focusRequester(focusRequester)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(Cine.PosterRatio)
                .scale(scale)
                .clip(RoundedCornerShape(6.dp))
                .background(show.tone)
                .onFocusChanged { focused = it.isFocused }
                .focusable(),
        )

        Spacer(Modifier.height(6.dp))

        Text(
            show.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (focused) Cine.TextPrimary else Cine.TextSecondary,
        )

        Spacer(Modifier.height(6.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (focused) Cine.FocusLine else Color.Transparent),
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(CircleShape)
            .background(if (focused) Color.White else Color(0xFFE8E8EA))
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun GhostButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(CircleShape)
            .background(if (focused) Color(0x33FFFFFF) else Color.Transparent)
            .border(1.dp, Color(0x55FFFFFF), CircleShape)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Text(label, color = Cine.TextPrimary, fontSize = 15.sp)
    }
}
