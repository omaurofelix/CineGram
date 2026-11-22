package com.cinegram.cine

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.Cine
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
private val PosterRowHeight = 240.dp

private enum class Tab(val media: String?) {
    Inicio("tv"),
    Series("tv"),
    Filmes("movie"),
    MinhaLista(null),
}

private fun tabOf(label: String): Tab = when (label) {
    "Séries" -> Tab.Series
    "Filmes" -> Tab.Filmes
    "Minha Lista" -> Tab.MinhaLista
    else -> Tab.Inicio
}

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
    val firstRowFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.Inicio) }
    var heroes by remember { mutableStateOf<List<Tmdb.Hero>>(emptyList()) }
    var trendingCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var popularCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var topCards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }
    var index by remember { mutableStateOf(0) }
    var lastAction by remember { mutableStateOf("-") }
    val logos = remember { mutableStateMapOf<String, String?>() }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }

    LaunchedEffect(tab) {
        scope.launch { listState.scrollToItem(0) }
        index = 0
        val media = tab.media ?: return@LaunchedEffect
        heroes = Tmdb.trending(media).take(MAX_HEROES)
        stills = if (tab == Tab.Inicio) Tmdb.stills(SHOW_ID, 1) else emptyList()
        popularCards = Tmdb.popular(media)
        topCards = Tmdb.topRated(media)
        trendingCards = heroes.map {
            Tmdb.Card(
                id = it.id,
                title = it.title,
                poster = null,
                backdrop = it.backdrop,
                overview = it.overview,
                isSeries = media == "tv",
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

    LaunchedEffect(tab, index, heroes.size) {
        val media = tab.media ?: return@LaunchedEffect
        val hero = heroes.getOrNull(index) ?: return@LaunchedEffect
        val key = "$media-${hero.id}"
        if (!logos.containsKey(key)) {
            logos[key] = Tmdb.logo(media, hero.id)
        }
    }

    Box(Modifier.fillMaxSize().background(HomeBackdrop)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 82.dp, bottom = 60.dp),
        ) {
            item {
                HeroCarousel(
                    heroes = heroes,
                    index = index,
                    media = tab.media ?: "tv",
                    lastAction = lastAction,
                    logos = logos,
                    upFocus = inicioFocus,
                    downFocus = firstRowFocus,
                    onDot = { index = it },
                    onStep = { delta ->
                        if (heroes.isNotEmpty()) {
                            index = (index + delta).coerceIn(0, heroes.lastIndex)
                        }
                    },
                    onPlay = { lastAction = "Assistir ${heroes.getOrNull(index)?.title.orEmpty()}" },
                )
            }

            if (tab == Tab.Inicio) {
                item {
                    Column {
                        Spacer(Modifier.height(24.dp))
                        RowTitle("Continuar assistindo")
                        Spacer(Modifier.height(10.dp))
                        RowSlot(EpisodeRowHeight) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                                contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
                            ) {
                                itemsIndexed(FakeRow, key = { _, item -> item.title }) { i, item ->
                                    LandscapeCard(
                                        item = item,
                                        url = stills.getOrNull(i),
                                        focusRequester = if (i == 0) firstRowFocus else null,
                                        onClick = { lastAction = item.title },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            when (tab) {
                Tab.MinhaLista -> item {
                    Column {
                        Spacer(Modifier.height(24.dp))
                        RowTitle("Minha Lista")
                        Spacer(Modifier.height(10.dp))
                        Placeholder("Os favoritos do perfil vão aparecer aqui.")
                    }
                }

                else -> {
                    item {
                        Column {
                            Spacer(Modifier.height(24.dp))
                            RowTitle("Em alta agora")
                            Spacer(Modifier.height(10.dp))
                            RowSlot(PosterRowHeight) {
                                PosterRow(trendingCards, firstRowFocus) { lastAction = it.title }
                            }
                        }
                    }

                    item {
                        Column {
                            Spacer(Modifier.height(22.dp))
                            RowTitle("Populares")
                            Spacer(Modifier.height(10.dp))
                            RowSlot(PosterRowHeight) {
                                PosterRow(popularCards) { lastAction = it.title }
                            }
                        }
                    }

                    item {
                        Column {
                            Spacer(Modifier.height(22.dp))
                            RowTitle("Mais bem avaliadas")
                            Spacer(Modifier.height(10.dp))
                            RowSlot(PosterRowHeight) {
                                PosterRow(topCards) { lastAction = it.title }
                            }
                        }
                    }
                }
            }
        }

        CineHeader(
            profile = active,
            firstFocus = inicioFocus,
            onNav = { tab = tabOf(it) },
            onSearch = { lastAction = "Buscar" },
            onProfile = { ProfileStore.setActive(null) },
        )
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
private fun Placeholder(text: String) {
    Box(
        Modifier.fillMaxWidth().height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Cine.TextSecondary)
    }
}

@Composable
private fun RowSlot(height: Dp, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
private fun HeroCarousel(
    heroes: List<Tmdb.Hero>,
    index: Int,
    media: String,
    lastAction: String,
    logos: Map<String, String?>,
    upFocus: FocusRequester,
    downFocus: FocusRequester,
    onDot: (Int) -> Unit,
    onStep: (Int) -> Unit,
    onPlay: () -> Unit,
) {
    val hero = heroes.getOrNull(index)
    val logo = hero?.let { logos["$media-${it.id}"] }
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
private fun PosterRow(
    cards: List<Tmdb.Card>,
    firstFocus: FocusRequester? = null,
    onClick: (Tmdb.Card) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
        contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
    ) {
        itemsIndexed(cards, key = { _, card -> card.id }) { index, card ->
            PosterCard(
                card = card,
                focusRequester = if (index == 0) firstFocus else null,
                onClick = { onClick(card) },
            )
        }
    }
}

@Composable
private fun PosterCard(card: Tmdb.Card, focusRequester: FocusRequester?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .width(150.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(10.dp))
            .background(Cine.Surface)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
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
