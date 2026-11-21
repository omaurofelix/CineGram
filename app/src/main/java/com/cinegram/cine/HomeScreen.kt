package com.cinegram.cine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.Cine
import kotlinx.coroutines.delay

private const val SHOW_ID = 133808

private data class FakeItem(val title: String, val tone: Color, val progress: Float)

private val FakeRow = listOf(
    FakeItem("Episódio 1", Color(0xFF2E3A45), 0.42f),
    FakeItem("Episódio 2", Color(0xFF3A2E33), 0f),
    FakeItem("Episódio 3", Color(0xFF2B3A2E), 0f),
    FakeItem("Episódio 4", Color(0xFF332F45), 0f),
    FakeItem("Episódio 5", Color(0xFF45382B), 0f),
    FakeItem("Episódio 6", Color(0xFF2A2A2A), 0f),
)

@Composable
fun HomeScreen() {
    val firstFocus = remember { FocusRequester() }
    var lastAction by remember { mutableStateOf("-") }
    var heroes by remember { mutableStateOf<List<Tmdb.Hero>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }

    LaunchedEffect(Unit) {
        heroes = Tmdb.trending()
        stills = Tmdb.stills(SHOW_ID, 1)
    }

    LaunchedEffect(heroes.size) {
        if (heroes.size > 1) {
            while (true) {
                delay(9000)
                index = (index + 1) % heroes.size
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Cine.Bg)) {
        CineHeader(
            profile = active,
            firstFocus = firstFocus,
            onNav = { lastAction = it },
            onSearch = { lastAction = "Buscar" },
            onProfile = { ProfileStore.setActive(null) },
        )

        Spacer(Modifier.height(6.dp))

        HeroCarousel(
            heroes = heroes,
            index = index,
            onDot = { index = it },
            onAction = { lastAction = it },
        )

        Spacer(Modifier.height(18.dp))

        Text(
            "Continuar assistindo",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
            color = Cine.TextPrimary,
            modifier = Modifier.padding(start = Cine.ScreenMargin),
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
            contentPadding = PaddingValues(horizontal = Cine.ScreenMargin, vertical = 6.dp),
        ) {
            itemsIndexed(FakeRow) { i, item ->
                LandscapeCard(
                    item = item,
                    url = stills.getOrNull(i),
                    firstFocus = null,
                    onClick = { lastAction = item.title },
                )
            }
        }

        Spacer(Modifier.height(10.dp))
    }

    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
}

@Composable
private fun HeroCarousel(
    heroes: List<Tmdb.Hero>,
    index: Int,
    onDot: (Int) -> Unit,
    onAction: (String) -> Unit,
) {
    val hero = heroes.getOrNull(index)

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Cine.ScreenMargin)
            .aspectRatio(2.6f)
            .clip(RoundedCornerShape(14.dp))
            .background(Cine.Surface),
    ) {
        if (hero?.backdrop != null) {
            AsyncImage(
                model = hero.backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Degrade da esquerda, para o texto ficar legivel sobre a arte.
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to Color(0xE6000000),
                        0.45f to Color(0x99000000),
                        0.75f to Color(0x11000000),
                    ),
                ),
            ),
        )

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 36.dp, end = 36.dp, bottom = 26.dp)
                .fillMaxWidth(0.56f),
        ) {
            Text(
                "DESTAQUE",
                style = MaterialTheme.typography.labelSmall,
                color = Cine.TextSecondary,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                hero?.title ?: "Carregando",
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                color = Cine.TextPrimary,
                maxLines = 1,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                hero?.overview?.takeIf { it.isNotBlank() }
                    ?: "Sem sinopse em português para este título.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.TextSecondary,
                maxLines = 2,
            )

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChipButton("Assistir", filled = true) { onAction("Assistir") }
                ChipButton("Detalhes", filled = false) { onAction("Detalhes") }
            }
        }

        if (heroes.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 22.dp, bottom = 22.dp),
            ) {
                heroes.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .size(if (i == index) 9.dp else 7.dp)
                            .clip(CircleShape)
                            .background(if (i == index) Cine.TextPrimary else Color(0x66FFFFFF))
                            .clickable { onDot(i) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipButton(label: String, filled: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f)
    val bg = when {
        filled -> if (focused) Color.White else Color(0xFFE9E9EC)
        focused -> Color(0x2EFFFFFF)
        else -> Color(0x1AFFFFFF)
    }

    Box(
        Modifier
            .offset(y = if (focused) (-2).dp else 0.dp)
            .scale(scale)
            .shadow(
                elevation = if (focused) 16.dp else 0.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(
                width = if (focused && !filled) 1.5.dp else if (filled) 0.dp else 1.dp,
                color = when {
                    filled -> Color.Transparent
                    focused -> Color(0xCCFFFFFF)
                    else -> Color(0x33FFFFFF)
                },
                shape = RoundedCornerShape(10.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
    ) {
        Text(
            label,
            color = if (filled) Color.Black else Cine.TextPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun LandscapeCard(
    item: FakeItem,
    url: String?,
    firstFocus: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f)

    Box(
        Modifier
            .width(196.dp)
            .aspectRatio(16f / 9f)
            .scale(scale)
            .shadow(
                elevation = if (focused) 14.dp else 0.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(RoundedCornerShape(10.dp))
            .background(item.tone)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            item.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = Cine.TextPrimary,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        )

        if (item.progress > 0f) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(item.progress)
                    .height(3.dp)
                    .background(Cine.Accent),
            )
        }
    }
}
