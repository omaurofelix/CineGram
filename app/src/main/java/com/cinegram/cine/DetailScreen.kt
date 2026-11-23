package com.cinegram.cine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.cine.profiles.Favorite
import com.cinegram.cine.profiles.FavoritesStore
import com.cinegram.design.Cine
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(card: Tmdb.Card, owner: String?, onBack: () -> Unit) {
    val favorites by FavoritesStore.items.collectAsState()
    val inList = favorites.any { it.owner == owner && it.media == card.media && it.id == card.id }
    val scope = rememberCoroutineScope()
    val firstButton = remember { FocusRequester() }
    var logo by remember { mutableStateOf<String?>(null) }
    var details by remember { mutableStateOf<Tmdb.Details?>(null) }
    var season by remember { mutableStateOf(1) }
    var episodes by remember { mutableStateOf<List<Tmdb.Episode>>(emptyList()) }

    LaunchedEffect(card.media, card.id) {
        logo = Tmdb.logo(card.media, card.id)
        details = Tmdb.details(card.media, card.id)
    }

    LaunchedEffect(card.media, card.id, season) {
        episodes = if (card.isSeries) Tmdb.season(card.id, season) else emptyList()
    }

    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Cine.Bg)) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    val art = card.backdrop ?: card.poster
                    if (art != null) {
                        AsyncImage(
                            model = art,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color(0x00000000),
                                    0.6f to Color(0xCC0A0A0A),
                                    1f to Cine.Bg,
                                ),
                            ),
                        ),
                    )
                }
            }

            item {
                Column(Modifier.padding(horizontal = Cine.ScreenMargin)) {
                    if (logo != null) {
                        AsyncImage(
                            model = logo,
                            contentDescription = card.title,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            modifier = Modifier.height(70.dp).fillMaxWidth(),
                        )
                    } else {
                        Text(
                            card.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Cine.TextPrimary,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        detailMeta(card, details),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                        color = Cine.TextPrimary,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        card.overview.ifBlank { "Sem sinopse em portugues para este titulo." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Cine.TextSecondary,
                    )

                    Spacer(Modifier.height(22.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActionButton(
                            label = "Assistir",
                            primary = true,
                            firstFocus = firstButton,
                        ) {}

                        ActionButton(
                            label = if (inList) "Na lista" else "Minha Lista",
                            primary = false,
                            checked = inList,
                            firstFocus = null,
                        ) {
                            scope.launch {
                                FavoritesStore.toggle(
                                    owner,
                                    Favorite(
                                        media = card.media,
                                        id = card.id,
                                        title = card.title,
                                        poster = card.poster,
                                        backdrop = card.backdrop,
                                        overview = card.overview,
                                        year = card.year,
                                        genres = card.genres,
                                    ),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))
                }
            }

            if (card.isSeries && episodes.isNotEmpty()) {
                item {
                    Text(
                        "Temporada $season",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
                        color = Cine.TextPrimary,
                        modifier = Modifier.padding(horizontal = Cine.ScreenMargin),
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Cine.ScreenMargin),
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
                        items(episodes, key = { it.number }) { episode ->
                            EpisodeCard(episode)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { runCatching { firstButton.requestFocus() } }
}

private fun detailMeta(card: Tmdb.Card, details: Tmdb.Details?): String {
    val parts = mutableListOf<String>()
    if (details != null && details.rating > 0) parts += "${"%.1f".format(details.rating)}  ★"
    if (card.isSeries) {
        if (details != null && details.seasons > 0) {
            parts += if (details.seasons == 1) "1 temporada" else "${details.seasons} temporadas"
        }
    } else if (details?.runtime != null) {
        parts += "${details.runtime / 60}h ${details.runtime % 60}min"
    }
    if (card.year != null) parts += card.year
    if (card.genres.isNotBlank()) parts += card.genres
    return parts.joinToString("  ·  ")
}

@Composable
private fun EpisodeCard(episode: Tmdb.Episode) {
    var focused by remember { mutableStateOf(false) }

    Column(
        Modifier
            .width(190.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable {}
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Cine.Surface),
        ) {
            if (episode.still != null) {
                AsyncImage(
                    model = episode.still,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            "${episode.number}. ${episode.name}",
            color = Cine.TextPrimary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    primary: Boolean,
    checked: Boolean = false,
    firstFocus: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape)
            .background(if (primary) Color(0xFFE9E9EC) else Cine.Pill)
            .border(
                width = if (focused) 3.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = shape,
            )
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 11.dp),
    ) {
        val tint = if (primary) Color.Black else Cine.TextPrimary
        if (primary) {
            PlayGlyph(tint)
        } else {
            ListGlyph(tint, checked)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            color = tint,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PlayGlyph(tint: Color) {
    Canvas(Modifier.size(11.dp, 12.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
private fun ListGlyph(tint: Color, checked: Boolean) {
    Canvas(Modifier.size(12.dp)) {
        val stroke = 1.8.dp.toPx()
        if (checked) {
            drawLine(
                tint,
                Offset(size.width * 0.15f, size.height * 0.55f),
                Offset(size.width * 0.42f, size.height * 0.82f),
                stroke,
                StrokeCap.Round,
            )
            drawLine(
                tint,
                Offset(size.width * 0.42f, size.height * 0.82f),
                Offset(size.width * 0.88f, size.height * 0.2f),
                stroke,
                StrokeCap.Round,
            )
        } else {
            drawLine(
                tint,
                Offset(size.width / 2f, 0f),
                Offset(size.width / 2f, size.height),
                stroke,
                StrokeCap.Round,
            )
            drawLine(
                tint,
                Offset(0f, size.height / 2f),
                Offset(size.width, size.height / 2f),
                stroke,
                StrokeCap.Round,
            )
        }
    }
}
