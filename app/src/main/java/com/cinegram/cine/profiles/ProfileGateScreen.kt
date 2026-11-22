package com.cinegram.cine.profiles

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tmplayer.R
import kotlinx.coroutines.launch
import java.util.UUID

private val AvatarFaces = listOf(
    Color(0xFF4FA8F5) to Color(0xFF0B5FC0),
    Color(0xFF3E8F9E) to Color(0xFF0B2E38),
    Color(0xFFF0413C) to Color(0xFFA8121E),
    Color(0xFFF5C443) to Color(0xFFC08A0B),
    Color(0xFF4BC98A) to Color(0xFF12805A),
    Color(0xFFA06BF5) to Color(0xFF4A1EA8),
)

private val GateBackground = Color(0xFF1E1E1E)
private val NetflixGray = Color(0xFFB3B3B3)
private val AddCircleGray = Color(0xFF3A3A3A)
private val AddCrossGray = Color(0xFFB8B8B8)
private val TileSize = 118.dp
private val TileSlot = 126.dp
private val AddCircleSize = 80.dp

@Composable
fun ProfileGateScreen() {
    val profiles by ProfileStore.profiles.collectAsState()
    val firstFocus = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    var creating by remember { mutableStateOf(false) }
    var managing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(GateBackground)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.fillMaxWidth().height(96.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.cinegram_logo),
                    contentDescription = "CineGram",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Quem está assistindo?",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                profiles.forEach { profile ->
                    ProfileTile(
                        profile = profile,
                        firstFocus = if (profile == profiles.first()) firstFocus else null,
                        onClick = { ProfileStore.setActive(profile.id) },
                    )
                }

                if (profiles.size < ProfileStore.MAX_PROFILES) {
                    AddTile(onClick = { creating = true })
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

    if (creating) {
        CreateDialog(
            onDismiss = { creating = false },
            onCreate = { name, kid ->
                scope.launch {
                    ProfileStore.add(
                        Profile(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            avatar = profiles.size % AvatarFaces.size,
                            kid = kid,
                            pin = null,
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
private fun ProfileTile(profile: Profile, firstFocus: FocusRequester?, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "profileScale")
    val face = AvatarFaces[profile.avatar % AvatarFaces.size]

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(TileSlot)
                .height(TileSlot)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(
                    width = 3.dp,
                    color = if (focused) Color.White else Color.Transparent,
                    shape = RectangleShape,
                )
                .onFocusChanged { focused = it.isFocused }
                .then(if (firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(TileSize)
                    .background(
                        Brush.verticalGradient(listOf(face.first, face.second)),
                        RectangleShape,
                    ),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val eye = w * 0.055f
                    drawCircle(Color.White, eye, Offset(w * 0.30f, h * 0.30f))
                    drawCircle(Color.White, eye, Offset(w * 0.70f, h * 0.30f))
                    val r = w * 0.23f
                    drawArc(
                        color = Color.White,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(w * 0.5f - r, h * 0.40f - r),
                        size = Size(r * 2, r * 2),
                        style = Stroke(width = w * 0.022f, cap = StrokeCap.Round),
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            profile.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = if (focused) Color.White else NetflixGray,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "addScale")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .width(TileSlot)
                .height(TileSlot)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .onFocusChanged { focused = it.isFocused }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(AddCircleSize)
                    .clip(CircleShape)
                    .background(AddCircleGray)
                    .then(
                        if (focused) {
                            Modifier.border(3.dp, Color.White, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val thickness = w * 0.11f
                    val arm = w * 0.22f
                    drawLine(
                        AddCrossGray,
                        Offset(w * 0.5f, h * 0.5f - arm),
                        Offset(w * 0.5f, h * 0.5f + arm),
                        strokeWidth = thickness,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        AddCrossGray,
                        Offset(w * 0.5f - arm, h * 0.5f),
                        Offset(w * 0.5f + arm, h * 0.5f),
                        strokeWidth = thickness,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Adicionar perfil",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = if (focused) Color.White else NetflixGray,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ManageButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .background(if (focused) Color.White else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (focused) Color.White else NetflixGray,
                shape = RectangleShape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 12.dp),
    ) {
        Text(
            "GERENCIAR PERFIS",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, letterSpacing = 2.sp),
            color = if (focused) Color.Black else NetflixGray,
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
                        Text(profile.name, color = Color.White)
                        TextButton(onClick = { onRemove(profile) }) { Text("Excluir") }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } },
    )
}

@Composable
private fun CreateDialog(onDismiss: () -> Unit, onCreate: (String, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var kid by remember { mutableStateOf(false) }

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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name.trim(), kid) },
                enabled = name.isNotBlank(),
            ) { Text("Criar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
