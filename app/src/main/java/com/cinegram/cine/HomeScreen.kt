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
    var heroUrl by remember { mutableStateOf<String?>(null) }
    var stills by remember { mutableStateOf<List<String?>>(emptyList()) }

    val profiles by ProfileStore.profiles.collectAsState()
    val activeId by ProfileStore.activeId.collectAsState()
    val active = profiles.firstOrNull { it.id == activeId }

    LaunchedEffect(Unit) {
        heroUrl = Tmdb.backdrop(SHOW_ID)
        stills = Tmdb.stills(SHOW_ID, 1)
    }

    Column(Modifier.fillMaxSize().background(Cine.Bg)) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            val hero = heroUrl
            if (hero != null) {
                AsyncImage(
                    model = hero,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFF2A3842),
                                0.55f to Color(0xCC10161B),
                                1f to Cine.Bg,
                            ),
                        ),
                    ),
                )
            }

            // O escurecimento por cima, para o texto ficar legível sobre a arte.
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color(0x33000000),
                            0.5f to Color(0x88000000),
                            1f to Cine.Bg,
                        ),
                    ),
                ),
            )

            CineHeader(
                profile = active,
                firstFocus = firstFocus,
                onNav = { lastAction = it },
                onSearch = { lastAction = "Buscar" },
                onProfile = { ProfileStore.setActive(null) },
            )

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = Cine.ScreenMargin, end = Cine.ScreenMargin, bottom = 18.dp)
                    .fillMaxWidth(0.58f),
            ) {
                Text(
                    "CINEGRAM",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cine.TextSecondary,
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    "Tom Tem Dez Anos",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 46.sp),
                    color = Cine.TextPrimary,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "2021  ·  16  ·  2 temporadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cine.TextSecondary,
                )

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton("Assistir") { lastAction = "Assistir" }
                    GhostButton("Detalhes") { lastAction = "Detalhes" }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    "Depois de um evento catastrófico, uma pequena comunidade luta para sobreviver num mundo onde a escuridão é o maior perigo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cine.TextSecondary,
                    maxLines = 3,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "último toque: $lastAction",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cine.TextSecondary,
                )
            }
        }

        Text(
            "Continuar assistindo",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp),
            color = Cine.TextPrimary,
            modifier = Modifier.padding(start = Cine.ScreenMargin, top = 6.dp),
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Cine.RowGap),
            contentPadding = PaddingValues(horizontal = Cine.ScreenMargin, vertical = 6.dp),
        ) {
            itemsIndexed(FakeRow) { index, item ->
                LandscapeCard(
                    item = item,
                    url = stills.getOrNull(index),
                    firstFocus = null,
                    onClick = { lastAction = item.title },
                )
            }
        }

        Spacer(Modifier.height(14.dp))
    }

    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
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
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(RoundedCornerShape(8.dp))
            .background(item.tone)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
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

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f)

    Box(
        Modifier
            .offset(y = if (focused) (-2).dp else 0.dp)
            .scale(scale)
            .shadow(
                elevation = if (focused) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(CircleShape)
            .background(Color(0xFFE9E9EC))
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 9.dp),
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun GhostButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f)

    Box(
        Modifier
            .offset(y = if (focused) (-2).dp else 0.dp)
            .scale(scale)
            .shadow(
                elevation = if (focused) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .clip(CircleShape)
            .background(if (focused) Color(0x2EFFFFFF) else Color(0x14FFFFFF))
            .border(
                width = if (focused) 1.5.dp else 1.dp,
                color = if (focused) Color(0xCCFFFFFF) else Color(0x33FFFFFF),
                shape = CircleShape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 9.dp),
    ) {
        Text(label, color = Cine.TextPrimary, fontSize = 13.sp)
    }
}
