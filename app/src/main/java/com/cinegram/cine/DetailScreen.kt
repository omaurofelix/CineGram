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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun DetailScreen(card: Tmdb.Card, media: String, owner: String?, onBack: () -> Unit) {
    val favorites by FavoritesStore.items.collectAsState()
    val inList = favorites.any { it.owner == owner && it.media == media && it.id == card.id }
    val scope = rememberCoroutineScope()
    val firstButton = remember { FocusRequester() }
    var logo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(media, card.id) {
        logo = Tmdb.logo(media, card.id)
    }

    BackHandler { onBack() }

    Box(Modifier.fillMaxSize().background(Cine.Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
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
                                0.55f to Color(0xCC0B0B0D),
                                1f to Cine.Bg,
                            ),
                        ),
                    ),
                )
            }

            Column(Modifier.padding(horizontal = Cine.ScreenMargin, vertical = 18.dp)) {
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
                    card.meta,
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

                Spacer(Modifier.height(24.dp))

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
                                    media = media,
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
    }

    LaunchedEffect(Unit) { runCatching { firstButton.requestFocus() } }
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
            .background(if (primary) Color(0xFFE9E9EC) else Color(0x33FFFFFF))
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
