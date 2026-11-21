package com.cinegram.cine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinegram.cine.profiles.Profile
import com.cinegram.design.Cine
import com.tmplayer.R

private val NavItems = listOf("Início", "Categorias", "Filmes", "Séries", "Favoritos")

private val ChipShape = RoundedCornerShape(10.dp)
private val ChipFocusBg = Color(0xFFE9E9EC)

/**
 * Barra superior do JetStream: avatar primeiro, navegacao no meio, busca e a
 * marca no canto oposto. Fica em barra solida, fora da arte.
 */
@Composable
fun CineHeader(
    profile: Profile?,
    firstFocus: FocusRequester?,
    onNav: (String) -> Unit,
    onSearch: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Cine.Bg)
            .padding(start = Cine.ScreenMargin, end = Cine.ScreenMargin, top = 22.dp, bottom = 12.dp),
    ) {
        Avatar(profile = profile, onClick = onProfile)

        Spacer(Modifier.width(22.dp))

        NavItems.forEachIndexed { index, label ->
            NavItem(
                label = label,
                firstFocus = if (index == 0) firstFocus else null,
                onClick = { onNav(label) },
            )
            Spacer(Modifier.width(8.dp))
        }

        Spacer(Modifier.weight(1f))

        RoundButton(onClick = onSearch) { tint -> SearchGlyph(tint) }

        Spacer(Modifier.width(18.dp))

        Brand()
    }
}

@Composable
private fun Brand() {
    Box(Modifier.width(120.dp).height(30.dp)) {
        Image(
            painter = painterResource(R.drawable.cinegram_logo),
            contentDescription = "CineGram",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NavItem(label: String, firstFocus: FocusRequester?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(ChipShape)
            .background(if (focused) ChipFocusBg else Color.Transparent)
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            color = if (focused) Color.Black else Cine.TextSecondary,
            fontSize = 14.sp,
            fontWeight = if (focused) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
private fun RoundButton(onClick: () -> Unit, content: @Composable (Color) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f)

    Box(
        Modifier
            .size(38.dp)
            .scale(scale)
            .clip(ChipShape)
            .background(if (focused) ChipFocusBg else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content(if (focused) Color.Black else Cine.TextSecondary)
    }
}

@Composable
private fun SearchGlyph(tint: Color) {
    Canvas(Modifier.size(14.dp)) {
        val stroke = 1.6.dp.toPx()
        drawCircle(
            color = tint,
            radius = size.minDimension / 2.6f,
            style = Stroke(width = stroke),
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.66f, size.height * 0.66f),
            end = Offset(size.width, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun Avatar(profile: Profile?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (profile == null) Cine.Surface else Cine.Accent)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) Cine.TextPrimary else Color(0x33FFFFFF),
                shape = CircleShape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            profile?.name?.take(1)?.uppercase() ?: "?",
            color = Cine.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
