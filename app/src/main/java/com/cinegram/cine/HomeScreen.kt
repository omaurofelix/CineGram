package com.cinegram.cine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.cine.profiles.Favorite
import com.cinegram.cine.profiles.FavoritesStore
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.Cine
import kotlinx.coroutines.launch

private const val SHOW_ID = 133808

private enum class Tab {
    Inicio,
    Series,
    Filmes,
    MinhaLista,
}

private fun tabOf(label: String): Tab = when (label) {
    "Séries" -> Tab.Series
    "Filmes" -> Tab.Filmes
    "Minha Lista" -> Tab.MinhaLista
    else -> Tab.Inicio
}

private fun favCard(fav: Favorite): Tmdb.Card = Tmdb.Card(
    id = fav.id,
    title = fav.title,
    poster = fav.poster,
    backdrop = fav.backdrop,
    overview = fav.overview,
    year = fav.year,
    genres = fav.genres,
    isSeries = fav.media == "tv",
)

/** Amostra temporária, só para ver a faixa montada antes da biblioteca do Telegram. */
private data class Sample(
    val id: Int,
    val title: String,
    val year: String,
    val line: String,
    val service: String,
)

private val SampleSeries = listOf(
    Sample(100088, "The Last of Us", "2023", "2 temporadas", "Max"),
    Sample(94997, "A Casa do Dragão", "2022", "2 temporadas", "Max"),
    Sample(97546, "Ted Lasso", "2020", "3 temporadas", "Apple TV"),
    Sample(66732, "Stranger Things", "2016", "4 temporadas", "Netflix"),
    Sample(93405, "Round 6", "2021", "3 temporadas", "Netflix"),
    Sample(76479, "The Boys", "2019", "4 temporadas", "Prime Video"),
    Sample(106379, "Fallout", "2024", "2 temporadas", "Prime Video"),
    Sample(87108, "Chernobyl", "2019", "1 temporada", "Max"),
)

private val SampleMovies = listOf(
    Sample(693134, "Duna: Parte 2", "2024", "2h 46min", "Max"),
    Sample(872585, "Oppenheimer", "2023", "3h", "Prime Video"),
    Sample(346698, "Barbie", "2023", "1h 54min", "Max"),
    Sample(157336, "Interestelar", "2014", "2h 49min", "Prime Video"),
    Sample(496243, "Parasita", "2019", "2h 12min", "Max"),
    Sample(598, "Cidade de Deus", "2002", "2h 10min", "Netflix"),
    Sample(244786, "Whiplash", "2014", "1h 47min", "Netflix"),
    Sample(603, "Matrix", "1999", "2h 16min", "Max"),
)

private suspend fun loadSamples(tab: Tab): List<Tmdb.Card> {
    val source = when (tab) {
        Tab.Filmes -> SampleMovies
        Tab.Series -> SampleSeries
        else -> SampleSeries.take(4) + SampleMovies.take(4)
    }
    return source.map { sample ->
        Tmdb.Card(
            id = sample.id,
            title = sample.title,
            poster = Tmdb.poster(if (tab == Tab.Filmes) "movie" else "tv", sample.id),
            year = sample.year,
            genres = sample.line,
            isSeries = tab != Tab.Filmes,
            service = sample.service,
        )
    }
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
    val rowFocus = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(Tab.Inicio) }
    var cards by remember { mutableStateOf<List<Tmdb.Card>>(emptyList()) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }
    var lastAction by remember { mutableStateOf("-") }
    var detail by remember { mutableStateOf<Tmdb.Card?>(null) }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }
    val favorites by FavoritesStore.items.collectAsState()
    val myList = favorites.filter { it.owner == activeId }

    LaunchedEffect(tab) {
        scope.launch { listState.scrollToItem(0) }
        cards = when (tab) {
            Tab.MinhaLista -> myList.map { favCard(it) }
            else -> loadSamples(tab)
        }
        stills = if (tab == Tab.Inicio) Tmdb.stills(SHOW_ID, 1) else emptyList()
    }

    LaunchedEffect(activeId, tab) {
        if (tab == Tab.MinhaLista) cards = myList.map { favCard(it) }
    }

    Box(Modifier.fillMaxSize().background(Cine.Bg)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 76.dp, bottom = 40.dp),
        ) {
            if (tab == Tab.Inicio) {
                item {
                    Column {
                        Spacer(Modifier.height(6.dp))
                        SectionTitle("Continuar assistindo")
                        Spacer(Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                            contentPadding = PaddingValues(horizontal = Cine.ScreenMargin),
                        ) {
                            itemsIndexed(FakeRow, key = { _, item -> item.title }) { i, item ->
                                LandscapeCard(
                                    item = item,
                                    url = stills.getOrNull(i),
                                    focusRequester = if (i == 0) inicioFocus else null,
                                    onClick = { lastAction = item.title },
                                )
                            }
                        }
                        Spacer(Modifier.height(26.dp))
                    }
                }
            } else {
                item { Spacer(Modifier.height(10.dp)) }
            }

            item {
                Column {
                    SectionTitle(
                        when (tab) {
                            Tab.Inicio -> "Em alta"
                            Tab.Series -> "Séries"
                            Tab.Filmes -> "Filmes"
                            Tab.MinhaLista -> "Minha Lista"
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    CoverRow(
                        cards = cards,
                        firstFocus = rowFocus,
                        onClick = { card -> detail = card },
                    )
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

        detail?.let { card ->
            DetailScreen(
                card = card,
                owner = activeId,
                onBack = { detail = null },
            )
        }
    }

    LaunchedEffect(Unit) { runCatching { inicioFocus.requestFocus() } }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
        color = Cine.TextPrimary,
        modifier = Modifier.padding(horizontal = Cine.ScreenMargin),
    )
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
