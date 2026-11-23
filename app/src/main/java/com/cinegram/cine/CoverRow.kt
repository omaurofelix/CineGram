package com.cinegram.cine

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.cinegram.design.Cine
import kotlinx.coroutines.launch

/**
 * Faixa de capas. O item central fica nitido e os vizinhos encolhem e escurecem,
 * como sombra. Anda pelo controle remoto, pelo dedo e pelo mouse, com encaixe.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoverRow(
    cards: List<Tmdb.Card>,
    firstFocus: FocusRequester?,
    onClick: (Tmdb.Card) -> Unit,
) {
    if (cards.isEmpty()) {
        Box(Modifier.fillMaxWidth().height(Cine.CardRowHeight), contentAlignment = Alignment.Center) {
            Text("Nada por aqui ainda.", style = MaterialTheme.typography.bodyMedium, color = Cine.TextSecondary)
        }
        return
    }

    val pager = rememberPagerState { cards.size }
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.collect { SoundTick.play() }
    }

    fun step(delta: Int) {
        val target = (pager.currentPage + delta).coerceIn(0, cards.lastIndex)
        if (target == pager.currentPage) return
        scope.launch {
            pager.animateScrollToPage(target, animationSpec = tween(Cine.SlideMs, easing = FastOutSlowInEasing))
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth().height(Cine.CardRowHeight)) {
        val side = ((maxWidth - Cine.CardWidth) / 2).coerceAtLeast(0.dp)

        HorizontalPager(
            state = pager,
            pageSpacing = Cine.RowGap,
            contentPadding = PaddingValues(horizontal = side),
            modifier = Modifier
                .fillMaxSize()
                .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft -> {
                            step(-1)
                            true
                        }
                        Key.DirectionRight -> {
                            step(1)
                            true
                        }
                        Key.DirectionCenter, Key.Enter -> {
                            cards.getOrNull(pager.currentPage)?.let(onClick)
                            true
                        }
                        else -> false
                    }
                },
        ) { page ->
            val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).let {
                if (it < 0) -it else it
            }
            val distance = offset.coerceAtMost(2f)
            val scale = 1f - (1f - Cine.ScaleFar) * (distance / 2f)
            val shadow = if (distance <= 1f) {
                Cine.ShadowNear * distance
            } else {
                Cine.ShadowNear + (Cine.ShadowFar - Cine.ShadowNear) * (distance - 1f)
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                Cover(
                    card = cards[page],
                    shadow = shadow,
                    focused = focused && page == pager.currentPage,
                    onClick = { onClick(cards[page]) },
                )
            }
        }
    }
}

@Composable
private fun Cover(card: Tmdb.Card, shadow: Float, focused: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(Cine.ShapeRadius)

    Box(
        Modifier
            .width(Cine.CardWidth)
            .aspectRatio(Cine.CardRatio)
            .clip(shape)
            .background(Cine.Surface)
            .focusProperties { canFocus = false }
            .clickable(onClick = onClick),
    ) {
        if (card.poster != null) {
            AsyncImage(
                model = card.poster,
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = shadow)))

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.55f to Color(0x00000000),
                            1f to Color(0xB3000000),
                        ),
                    ),
                ),
        )

        if (card.service != null) {
            ServiceBadge(card.service, Modifier.align(Alignment.TopStart).padding(10.dp))
        }

        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(
                card.title,
                color = Cine.TextPrimary,
                fontSize = 15.sp,
                fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 2,
            )
            if (card.meta.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Cine.Pill)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Text(card.meta, color = Cine.TextPrimary, fontSize = 11.sp)
                }
            }
        }
    }
}

/** Selo do canal de origem. Sem logo, usa a inicial sobre a cor do canal. */
@Composable
fun ServiceBadge(service: String, modifier: Modifier = Modifier) {
    val tone = serviceTone(service)
    Box(
        modifier
            .clip(RoundedCornerShape(7.dp))
            .background(tone)
            .padding(horizontal = 7.dp, vertical = 4.dp),
    ) {
        Text(
            service.take(1).uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun serviceTone(service: String): Color {
    val palette = listOf(
        Color(0xFFE50914),
        Color(0xFF5A2D91),
        Color(0xFF1F80E0),
        Color(0xFF0FA36B),
        Color(0xFFE8A33D),
        Color(0xFFB5121B),
    )
    val index = (service.hashCode() and Int.MAX_VALUE) % palette.size
    return palette[index]
}
