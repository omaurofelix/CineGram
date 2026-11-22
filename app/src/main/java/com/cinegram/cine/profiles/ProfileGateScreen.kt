package com.cinegram.cine.profiles

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cinegram.design.Cine
import com.tmplayer.R
import kotlinx.coroutines.launch
import java.util.UUID

private val AvatarColors = listOf(
    Color(0xFF3B4A5A),
    Color(0xFF5A3B45),
    Color(0xFF3B5A44),
    Color(0xFF4A3B5A),
    Color(0xFF5A4F3B),
    Color(0xFF3B525A),
)

private val GateLight = Color(0xFFD6E0EB)
private val GateLightFocused = Color(0xFFFFFFFF)
private val GateInk = Color(0xFF0D1B2B)

private val GateBackdrop = Brush.verticalGradient(
    colorStops = arrayOf(
        0f to Color(0xFF1B4C86),
        0.28f to Color(0xFF14325A),
        0.6f to Color(0xFF0C1A2B),
        1f to Cine.Bg,
    ),
)

@Composable
fun ProfileGateScreen() {
    val profiles by ProfileStore.profiles.collectAsState()
    val firstFocus = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    var askingPin by remember { mutableStateOf<Profile?>(null) }
    var creating by remember { mutableStateOf(false) }
    var managing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(GateBackdrop)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = Cine.ScreenMargin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(46.dp))

            Box(
                Modifier.fillMaxWidth().height(46.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.cinegram_logo),
                    contentDescription = "CineGram",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.height(30.dp))

            Text(
                "Quem está assistindo?",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 34.sp),
                color = Cine.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.Top,
            ) {
                profiles.forEach { profile ->
                    ProfileCard(
                        profile = profile,
                        firstFocus = if (profile == profiles.first()) firstFocus else null,
                        onClick = {
                            if (profile.pin.isNullOrEmpty()) {
                                ProfileStore.setActive(profile.id)
                            } else {
                                askingPin = profile
                            }
                        },
                    )
                }

                if (profiles.size < ProfileStore.MAX_PROFILES) {
                    AddCard(onClick = { creating = true })
                }
            }

            Spacer(Modifier.height(44.dp))

            ManageButton(onClick = { managing = true })
        }
    }

    LaunchedEffect(profiles.isNotEmpty()) {
        if (profiles.isNotEmpty()) {
            runCatching { firstFocus.requestFocus() }
        }
    }

    askingPin?.let { profile ->
        PinDialog(
            profile = profile,
            onDismiss = { askingPin = null },
            onCorrect = {
                ProfileStore.setActive(profile.id)
                askingPin = null
            },
        )
    }

    if (creating) {
        CreateDialog(
            onDismiss = { creating = false },
            onCreate = { name, kid, pin ->
                scope.launch {
                    ProfileStore.add(
                        Profile(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            avatar = profiles.size % AvatarColors.size,
                            kid = kid,
                            pin = pin,
                        ),
                    )
                }
                creating = false
            },
        )
    }

    if (managing) {
        ManageDialog(
            profiles = profiles,
            onDismiss = { managing = false },
            onRemove = { profile -> scope.launch { ProfileStore.remove(profile.id) } },
        )
    }
}

@Composable
private fun ProfileCard(profile: Profile, firstFocus: FocusRequester?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "profileScale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(132.dp),
    ) {
        Box(
            Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(AvatarColors[profile.avatar % AvatarColors.size])
                .border(
                    width = if (focused) Cine.FocusRingWidth else 0.dp,
                    color = if (focused) Cine.FocusRing else Color.Transparent,
                    shape = CircleShape,
                )
                .onFocusChanged { focused = it.isFocused }
                .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                profile.name.take(1).uppercase(),
                fontSize = 34.sp,
                color = Cine.TextPrimary,
                fontWeight = FontWeight.Light,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            profile.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (focused) Cine.TextPrimary else Cine.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddCard(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "addScale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(132.dp),
    ) {
        Box(
            Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(if (focused) GateLightFocused else GateLight)
                .border(
                    width = if (focused) Cine.FocusRingWidth else 0.dp,
                    color = if (focused) GateInk else Color.Transparent,
                    shape = CircleShape,
                )
                .onFocusChanged { focused = it.isFocused }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "+",
                fontSize = 44.sp,
                color = GateInk,
                fontWeight = FontWeight.Light,
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Adicionar perfil",
            style = MaterialTheme.typography.bodyMedium,
            color = if (focused) Cine.TextPrimary else Cine.TextSecondary,
        )
    }
}

@Composable
private fun ManageButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (focused) GateLightFocused else GateLight)
            .border(
                width = if (focused) Cine.FocusRingWidth else 0.dp,
                color = if (focused) GateInk else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 13.dp),
    ) {
        Text(
            "GERENCIAR PERFIS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, letterSpacing = 1.6.sp),
            color = GateInk,
        )
    }
}

@Composable
private fun ManageDialog(profiles: List<Profile>, onDismiss: () -> Unit, onRemove: (Profile) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerenciar perfis") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                profiles.forEach { profile ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(profile.name, color = Cine.TextPrimary)
                        TextButton(onClick = { onRemove(profile) }) { Text("Excluir") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

@Composable
private fun PinDialog(profile: Profile, onDismiss: () -> Unit, onCorrect: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val pin = profile.pin.orEmpty()

    LaunchedEffect(typed) {
        if (typed.length == pin.length) {
            if (typed == pin) onCorrect() else typed = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PIN de ${profile.name}") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "\u2022".repeat(typed.length).padEnd(pin.length, '\u00b7'),
                    color = Cine.TextPrimary,
                )
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("123", "456", "789", "0").forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { digit ->
                                TextButton(onClick = { typed += digit }) {
                                    Text(digit.toString(), fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun CreateDialog(onDismiss: () -> Unit, onCreate: (String, Boolean, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kid by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo perfil") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = kid, onCheckedChange = { kid = it })
                    Spacer(Modifier.width(12.dp))
                    Text("Perfil infantil")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it.filter(Char::isDigit) },
                    label = { Text("PIN de 4 dígitos (opcional)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), kid, pin.ifEmpty { null }) },
                enabled = name.isNotBlank(),
            ) { Text("Criar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
