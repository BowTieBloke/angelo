package de.arschwasser.angelo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.core.SongDatabase
import de.arschwasser.angelo.player.MusicServiceRegistry
import de.arschwasser.angelo.qrscanner.PermissionedQRScanner
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var songsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            songsLoaded = SongDatabase.get(ctx).songDao().count() > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Angelo") },
                actions = {
                    IconButton(onClick = { nav.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { padding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!songsLoaded) {
                Button(onClick = { nav.navigate("selectVersion") }) {
                    Text("Select game version")
                }
            } else if (!scanning) {
                Button(
                    modifier = Modifier
                        .size(200.dp, 200.dp),
                    onClick = { scanning = true }
                ) {
                    Text("Scan")
                }
            } else {
                PermissionedQRScanner(
                    onCode = { qr ->
                        scanning = false
                        scope.launch {
                            val code = if (qr.contains("c=")) qr.substringAfter("c=") else qr
                            val song = SongDatabase.get(ctx).songDao().findByCode(code)
                            if (song != null) {
                                val pref = PreferencesManager(ctx)
                                val preferred = pref.serviceFlow.firstOrNull()
                                val service = MusicServiceRegistry.availableServices(ctx)
                                    .find { it.name == preferred }
                                    ?: MusicServiceRegistry.availableServices(ctx).firstOrNull()
                                if (service == null) {
                                    Toast.makeText(
                                        ctx,
                                        "No music service available",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@launch
                                }
                                val ok = service.play(ctx, song)
                                if (!ok) Toast.makeText(
                                    ctx,
                                    "Could not play song",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(ctx, "Song not found", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onCancel = {
                        scanning = false
                        Toast.makeText(ctx, "QR scan cancelled", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
