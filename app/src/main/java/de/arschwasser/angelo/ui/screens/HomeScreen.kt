package de.arschwasser.angelo.ui.screens

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.core.SongDatabase
import de.arschwasser.angelo.player.MusicServiceRegistry
import de.arschwasser.angelo.qrscanner.PermissionedQRScanner
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import de.arschwasser.angelo.R

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer, // Start color
            MaterialTheme.colorScheme.onPrimary, // Middle color
            Color.Black  // End color
        )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) { content() }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController) {
    AppBackground {
        /* ───────────────────────── state ───────────────────────── */
        val view = LocalView.current
        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()
        var scanning by remember { mutableStateOf(false) }
        var songsLoaded by remember { mutableStateOf(false) }

        /* do we already have songs? */
        LaunchedEffect(Unit) {
            songsLoaded = SongDatabase.get(ctx).songDao().count() > 0
        }

        /* ───────────────────────── scaffold ────────────────────── */
        val barColor = MaterialTheme.colorScheme.primaryContainer
        val statusBar by animateColorAsState(barColor)          // nice fade if the colour changes

        /* edge to edge status bar colour */
        SideEffect {
            (view.context as? android.app.Activity)
                ?.window?.statusBarColor = statusBar.toArgb()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Angelo") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = barColor,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    actions = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            nav.navigate("settings")
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
        ) { padding ->
            AppBackground {
                /* ------- actual screen content ------- */
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .imePadding(),                         // plays nicely with keyboard
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    when {
                        /* first‑time users choose game version */
                        !songsLoaded -> {
                            FilledTonalButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    nav.navigate("selectVersion")
                                },
                                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                            ) { Text("Select game version") }
                        }

                        /* not scanning yet → show big button */
                        !scanning -> {
                            val gradient = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                            FilledTonalButton(
                                onClick = {
                                    scanning = true
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(CircleShape)
                                    .background(gradient)
                                    .border(4.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.angelo_note),
                                    contentDescription = "Scan QR code",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,   // any colour
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        /* camera preview */
                        else -> {
                            PermissionedQRScanner(
                                onCode = { qr ->
                                    scanning = false
                                    scope.launch {
                                        val code = qr.substringAfter("c=", qr)
                                        val song = SongDatabase.get(ctx).songDao().findByCode(code)
                                        if (song == null) {
                                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            Toast.makeText(ctx, "Song not found", Toast.LENGTH_LONG)
                                                .show()
                                            return@launch
                                        }
                                        val pref = PreferencesManager(ctx)
                                        val preferred = pref.serviceFlow.firstOrNull()
                                        val service = MusicServiceRegistry
                                            .availableServices(ctx)
                                            .find { it.name == preferred }
                                            ?: MusicServiceRegistry.availableServices(ctx).first()
                                        if (!service.play(ctx, song)) {
                                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            Toast.makeText(
                                                ctx,
                                                "Could not play song",
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        } else {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        }
                                    }
                                },
                                onCancel = {
                                    scanning = false
                                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                    Toast.makeText(ctx, "QR scan cancelled", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
