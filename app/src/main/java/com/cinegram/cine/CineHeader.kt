package com.cinegram.cine

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.cinegram.cine.profiles.Profile
import com.cinegram.cine.profiles.ProfileFace
import com.cinegram.cine.profiles.ProfileStore
import com.cinegram.design.Cine
import com.tmplayer.R

private val NavItems = listOf("Início", "Séries", "Filmes", "Minha Lista")

private val PillShape = RoundedCornerShape(50)
private val PillBg = Color(0xFFF5F5F7)
private val ArrowGray = Color(0xFFB3B3B3)
private val MenuBg = Color(0xFF232326)

@Composable
fun CineHeader(
    profile: Profile?,
    firstFocus: FocusRequester?,
    onNav: (String) -> Unit,
    onSearch: () -> Unit,
    onProfile: () -> Unit,
) {
    val profiles by ProfileStore.profiles.collectAsState()
    var switching by remember { mutableStateOf(false) }
    val dropFocus = remember { FocusRequester() }
    val others = profiles.filter { it.id != profile?.id }

    LaunchedEffect(switching) {
        if (switching) runCatching { dropFocus.requestFocus() }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = Cine.ScreenMargin, end = Cine.ScreenMargin, top = 20.dp, bottom = 14.dp),
    ) {
        Box(Modifier.align(Alignment.CenterStart)) {
            ProfileSwitch(
                profile = profile,
                onOpen = { switching = !switching },
            )

            if (switching) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, with(LocalDensity.current) { 50.dp.roundToPx() }),
                    onDismissRequest = { switching = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MenuBg)
                            .padding(8.dp),
                    ) {
                        others.forEachIndexed { index, other ->
                            MenuRow(
                                profile = other,
                                firstFocus = if (index == 0) dropFocus else null,
                                onClick = {
                                    ProfileStore.setActive(other.id)
                                    switching = false
                                },
                            )
                        }
                        MenuRow(
                            profile = null,
                            label = "Gerenciar perfis",
                            firstFocus = if (others.isEmpty()) dropFocus else null,
                            onClick = {
                                switching = false
                                onProfile()
                            },
                        )
                    }
                }
            }
        }

        Row(
            Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundButton(onClick = onSearch) { tint -> SearchGlyph(tint) }

            Spacer(Modifier.width(18.dp))

            NavItems.forEachIndexed { index, label ->
                NavItem(
                    label = label,
                    firstFocus = if (index == 0) firstFocus else null,
                    onClick = { onNav(label) },
                )
                Spacer(Modifier.width(6.dp))
            }
        }

        Brand(Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun ProfileSwitch(profile: Profile?, onOpen: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) Cine.TextPrimary else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onOpen)
            .padding(4.dp),
    ) {
        ProfileFace(avatar = profile?.avatar ?: 0, size = 38.dp, corner = 8.dp)
        Spacer(Modifier.width(8.dp))
        ArrowGlyph()
        Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun MenuRow(
    profile: Profile?,
    label: String? = null,
    firstFocus: FocusRequester?,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (focused) PillBg else Color.Transparent)
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        if (profile != null) {
            ProfileFace(avatar = profile.avatar, size = 26.dp, corner = 5.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            profile?.name ?: label.orEmpty(),
            color = if (focused) Color.Black else Cine.TextPrimary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun Brand(modifier: Modifier = Modifier) {
    Box(modifier.size(120.dp, 30.dp)) {
        Image(
            painter = painterResource(R.drawable.cinegram_logo),
            contentDescription = "CineGram",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun NavItem(label: String, firstFocus: FocusRequester?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(PillShape)
            .background(if (focused) PillBg else Color.Transparent)
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
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
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "searchScale")

    Box(
        Modifier
            .size(38.dp)
            .scale(scale)
            .clip(PillShape)
            .background(if (focused) PillBg else Color.Transparent)
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
private fun ArrowGlyph() {
    Canvas(Modifier.size(10.dp, 7.dp)) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        drawPath(path = path, color = ArrowGray, style = Fill)
    }
}
