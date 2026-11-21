package com.cinegram.cine.profiles

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
import androidx.compose.ui.graphics.Color
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

@Composable
fun ProfileGateScreen() {
    val profiles by ProfileStore.profiles.collectAsState()
    val firstFocus = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    var askingPin by remember { mutableStateOf<Profile?>(null) }
    var creating by remember { mutableStateOf(false) }
    var managing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Cine.Bg)) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(start = 56.dp, top = 26.dp)
                .width(210.dp)
                .height(52.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.cinegram_logo),
                contentDescription = "CineGram",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            Modifier.fillMaxSize().padding(top = 150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Quem está assistindo?",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 34.sp),
                color = Cine.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(42.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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

            Spacer(Modifier.height(54.dp))

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(140.dp)
            .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier),
    ) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AvatarColors[profile.avatar % AvatarColors.size])
                .border(
                    width = if (focused) Cine.FocusRingWidth else 0.dp,
                    color = if (focused) Cine.FocusRing else Color.Transparent,
                    shape = CircleShape,
                )
                .onFocusChanged { focused = it.isFocused }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                profile.name.take(1).uppercase(),
                fontSize = 40.sp,
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

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(140.dp)) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(if (focused) Color(0xFF3A3A40) else Color(0xFF2A2A2E))
                .border(
                    width = if (focused) Cine.FocusRingWidth else 0.dp,
                    color = if (focused) Cine.FocusRing else Color.Transparent,
                    shape = CircleShape,
                )
                .onFocusChanged { focused = it.isFocused }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 52.sp, color = Cine.TextPrimary, fontWeight = FontWeight.Light)
        }

        Spacer(Modifier.height(12.dp))

        Text("Adicionar perfil", style = MaterialTheme.typography.bodyMedium, color = Cine.TextSecondary)
    }
}

@Composable
private fun ManageButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .clip(RoundedCornerShape(2.dp))
            .border(
                width = 1.dp,
                color = if (focused) Cine.TextPrimary else Color(0x66FFFFFF),
                shape = RoundedCornerShape(2.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 11.dp),
    ) {
        Text(
            "GERENCIAR PERFIS",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp, letterSpacing = 1.6.sp),
            color = if (focused) Cine.TextPrimary else Cine.TextSecondary,
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
                    "•".repeat(typed.length).padEnd(pin.length, '·'),
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
