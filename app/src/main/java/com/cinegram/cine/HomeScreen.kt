package com.cinegram.cine

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.Cine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HomeBackdrop = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color(0xFF16243B),
        0.32f to Color(0xFF0A1220),
        1f to Color(0xFF000000),
    ),
)
private const val SHOW_ID = 133808
private const val MAX_HEROES = 6

private val EpisodeRowHeight = 138.dp
private val ExpandRowHeight = 340.dp
private val CardWidth = 150.dp
private val PosterHeight = 225.dp
private val ExpandedCardWidth = 440.dp
private val ExpandedHeight = 248.dp

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
    val inicioFocus = remember { FocusRequester() }
    val firstCardFocus = remember { FocusRequester() }
    var heroes by remember { mutableStateOf<List<Tmdb.Hero>>(emptyList()) }
    var trendingCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var popularCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var topCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var lastAction by remember { mutableStateOf("-") }
    val logos = remember { mutableStateMapOf<Int, String?>() }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }

    LaunchedEffect(Unit) {
        heroes = Tmdb.trending().take(MAX_HEROES)
        stills = Tmdb.stills(SHOW_ID, 1)
        popularCards = Tmdb.popular()
        topCards = Tmdb.topRated()
        trendingCards = heroes.map {
            Tmdb.Card(
                id = it.id,
                title = it.title,
                poster = null,
                backdrop = it.backdrop,
                overview = it.overview,
                isSeries = true,
            )
        }
    }

    LaunchedEffect(heroes.size) {
        if (heroes.size > 1) {
            while (true) {
                delay(9000)
                index = (index + 1) % heroes.size
            }
        }
    }

    LaunchedEffect(index, heroes.size) {
        val hero = heroes.getOrNull(index) ?: return@LaunchedEffect
        if (!logos.containsKey(hero.id)) {
            logos[hero.id] = Tmdb.logo(hero.id)
        }
    }

    Column(Modifier.fillMaxSize().background(HomeBackdrop)) {
        CineHeader(
            profile = active,
            firstFocus = inicioFocus,
            onNav = { lastAction = it },
            onSearch = { lastAction = "Buscar" },
            onProfile = { ProfileStore.setActive(null) },
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 60.dp),
        ) {
            item {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HeroCarousel(
                        heroes = heroes,
                        index = index,
                        lastAction = lastAction,
                        logos = logos,
                        upFocus = inicioFocus,
                        downFocus = firstCardFocus,
                        onDot = { index = it },
                        onStep = { delta ->
                            if (heroes.isNotEmpty()) {
                                index = (index + delta).coerceIn(0, heroes.lastIndex)
                            }
                        },
                        onPlay = { lastAction = "Assistir ${heroes.getOrNull(index)?.title.orEmpty()}" },
                    )
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(30.dp))
                    RowTitle("Continuar assistindo")
                    RowSlot(EpisodeRowHeight) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                            contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
                        ) {
                            itemsIndexed(FakeRow, key = { _, item -> item.title }) { i, item ->
                                LandscapeCard(
                                    item = item,
                                    url = stills.getOrNull(i),
                                    focusRequester = if (i == 0) firstCardFocus else null,
                                    onClick = { lastAction = item.title },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(26.dp))
                    RowTitle("Em alta agora")
                    RowSlot(ExpandRowHeight, Alignment.TopStart) {
                        ExpandRow(trendingCards, logos) { lastAction = it.title }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(26.dp))
                    RowTitle("Populares")
                    RowSlot(ExpandRowHeight, Alignment.TopStart) {
                        ExpandRow(popularCards, logos) { lastAction = it.title }
                    }
                }
            }

            item {
                Column {
                    Spacer(Modifier.height(26.dp))
                    RowTitle("Mais bem avaliadas")
                    RowSlot(ExpandRowHeight, Alignment.TopStart) {
                        ExpandRow(topCards, logos) { lastAction = it.title }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { runCatching { inicioFocus.requestFocus() } }
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
private fun RowSlot(
    height: Dp,
    alignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier.fillMaxWidth().height(height),
        contentAlignment = alignment,
    ) {
        content()
    }
}

@Composable
private fun HeroCarousel(
    heroes: List<Tmdb.Hero>,
    index: Int,
    lastAction: String,
    logos: Map<Int, String?>,
    upFocus: FocusRequester,
    downFocus: FocusRequester,
    onDot: (Int) -> Unit,
    onStep: (Int) -> Unit,
    onPlay: () -> Unit,
) {
    val hero = heroes.getOrNull(index)
    val logo = hero?.let { logos[it.id] }
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .aspectRatio(2.2f)
            .clip(RoundedCornerShape(14.dp))
            .background(Cine.Surface)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
            .focusProperties {
                up = upFocus
                down = downFocus
            }
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> {
                        onStep(-1)
                        true
                    }
                    Key.DirectionRight -> {
                        onStep(1)
                        true
                    }
                    else -> false
                }
            }
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
                .fillMaxWidth(0.62f),
        ) {
            Text("DESTAQUE", style = MaterialTheme.typography.labelSmall, color = Cine.TextSecondary)

            Spacer(Modifier.height(10.dp))

            if (logo != null) {
                AsyncImage(
                    model = logo,
                    contentDescription = hero?.title,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart,
                    modifier = Modifier.height(66.dp).fillMaxWidth(),
                )
            } else {
                Text(
                    hero?.title ?: "Carregando",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp),
                    color = Cine.TextPrimary,
                    maxLines = 2,
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                hero?.overview?.takeIf { it.isNotBlank() }
                    ?: "Sem sinopse em portugues para este titulo.",
                style = MaterialTheme.typography.bodyMedium,
                color = Cine.TextSecondary,
                maxLines = 2,
            )
        }

        if (heroes.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x4DFFFFFF))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                heroes.forEachIndexed { i, _ ->
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (i == index) Color.White else Color(0xB3FFFFFF))
                            .clickable { onDot(i) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandRow(
    cards: List<Tmdb.Card>,
    logos: MutableMap<Int, String?>,
    onClick: (Tmdb.Card) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
        contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
    ) {
        itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
            ExpandCard(
                card = card,
                index = index,
                listState = listState,
                logos = logos,
                onClick = { onClick(card) },
            )
        }
    }
}

@Composable
private fun ExpandCard(
    card: Tmdb.Card,
    index: Int,
    listState: LazyListState,
    logos: MutableMap<Int, String?>,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val logo = logos[card.id]

    val width by animateDpAsState(
        targetValue = if (expanded) ExpandedCardWidth else CardWidth,
        animationSpec = tween(180),
        label = "cardWidth",
    )
    val height by animateDpAsState(
        targetValue = if (expanded) ExpandedHeight else PosterHeight,
        animationSpec = tween(180),
        label = "cardHeight",
    )

    Column(Modifier.width(width)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(6.dp))
                .background(Cine.Surface)
                .border(
                    width = if (focused) 3.dp else 0.dp,
                    color = if (focused) Cine.TextPrimary else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                )
                .onFocusChanged { state ->
                    focused = state.isFocused
                    job?.cancel()
                    if (state.isFocused) {
                        job = scope.launch {
                            listState.animateScrollToItem(index)
                            if (!logos.containsKey(card.id)) {
                                logos[card.id] = Tmdb.logo(card.id)
                            }
                            expanded = true
                        }
                    } else {
                        expanded = false
                    }
                }
                .clickable(onClick = onClick),
        ) {
            val art = if (expanded) card.backdrop ?: card.poster else card.poster ?: card.backdrop
            if (art != null) {
                AsyncImage(
                    model = art,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (expanded) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.verticalGradient(listOf(Color(0x00000000), Color(0xD9000000))),
                        ),
                )

                if (logo != null) {
                    AsyncImage(
                        model = logo,
                        contentDescription = card.title,
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.CenterStart,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .height(34.dp)
                            .fillMaxWidth(0.7f)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                } else {
                    Text(
                        card.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = Cine.TextPrimary,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        if (expanded) {
            Column(Modifier.padding(top = 10.dp, end = 8.dp)) {
                Text(
                    card.meta,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = Cine.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    card.overview.ifBlank { "Sem sinopse em portugues para este titulo." },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = Cine.TextSecondary,
                    maxLines = 3,
                )
            }
        }
    }
}

@Composable
private fun LandscapeCard(
    item: FakeItem,
    url: String?,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .width(196.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(10.dp))
            .background(item.tone)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
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
