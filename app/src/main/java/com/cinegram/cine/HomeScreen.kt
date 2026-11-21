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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.focus.FocusRequester
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
    var heroes by remember { mutableStateOf<List<Tmdb.Hero>>(emptyList()) }
    var trendingCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var popularCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var topCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var lastAction by remember { mutableStateOf("-") }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }

    LaunchedEffect(Unit) {
        heroes = Tmdb.trending()
        stills = Tmdb.stills(SHOW_ID, 1)
        popularCards = Tmdb.popular()
        topCards = Tmdb.topRated()
        trendingCards = heroes.map { Tmdb.Card(it.id, it.title, null) }
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

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 30.dp),
        ) {
            item {
                Column {
                    Spacer(Modifier.height(18.dp))
                    HeroCarousel(
                        heroes = heroes,
                        index = index,
                        lastAction = lastAction,
                        onDot = { index = it },
                        onPlay = { lastAction = "Assistir ${heroes.getOrNull(index)?.title.orEmpty()}" },
                    )
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(34.dp))
                    RowTitle("Continuar assistindo")
                    Spacer(Modifier.height(14.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                        contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
                    ) {
                        itemsIndexed(FakeRow) { i, item ->
                            LandscapeCard(
                                item = item,
                                url = stills.getOrNull(i),
                                onClick = { lastAction = item.title },
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(30.dp))
                    RowTitle("Em alta agora")
                    Spacer(Modifier.height(14.dp))
                    PosterRow(trendingCards) { lastAction = it.title }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(30.dp))
                    RowTitle("Populares")
                    Spacer(Modifier.height(14.dp))
                    PosterRow(popularCards) { lastAction = it.title }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(30.dp))
                    RowTitle("Mais bem avaliadas")
                    Spacer(Modifier.height(14.dp))
                    PosterRow(topCards) { lastAction = it.title }
                }
            }
        }
    }

    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
}

@Composable
private fun RowTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
        color = Cine.TextPrimary,
        modifier = Modifier.padding(horizontal = Cine.ScreenMargin),
    )
}

@Composable
private fun HeroCarousel(
    heroes: List<Tmdb.Hero>,
    index: Int,
    lastAction: String,
    onDot: (Int) -> Unit,
    onPlay: () -> Unit,
) {
    val hero = heroes.getOrNull(index)
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Cine.ScreenMargin)
            .aspectRatio(2.6f)
            .clip(RoundedCornerShape(14.dp))
            .background(Cine.Surface)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onPlay),
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
            Text("DESTAQUE", style = MaterialTheme.typography.labelSmall, color = Cine.TextSecondary)

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
                    ?: "Sem sinopse em portugues para este titulo.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.TextSecondary,
                maxLines = 2,
            )

            if (focused) {
                Spacer(Modifier.height(16.dp))
                PlayBadge()
                Spacer(Modifier.height(10.dp))
                Text(
                    "selecionado: $lastAction",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cine.TextSecondary,
                )
            }
        }

        if (heroes.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 22.dp)
                    .clip(CircleShape)
                    .background(Color(0x4D000000))
                    .padding(horizontal = 11.dp, vertical = 8.dp),
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
private fun PlayBadge() {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE9E9EC))
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("▶", color = Color.Black, fontSize = 11.sp)
            Spacer(Modifier.width(9.dp))
            Text("Assistir", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PosterRow(cards: List<Tmdb.Card>, onClick: (Tmdb.Card) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
        contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
    ) {
        items(cards) { card ->
            PosterCard(card = card, onClick = { onClick(card) })
        }
    }
}

@Composable
private fun PosterCard(card: Tmdb.Card, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.05f else 1f)

    Box(
        Modifier
            .width(150.dp)
            .aspectRatio(2f / 3f)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(Cine.Surface)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
    ) {
        if (card.poster != null) {
            AsyncImage(
                model = card.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LandscapeCard(item: FakeItem, url: String?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f)

    Box(
        Modifier
            .width(196.dp)
            .aspectRatio(16f / 9f)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(item.tone)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
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
