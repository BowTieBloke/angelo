package de.arschwasser.angelo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.player.MusicServiceRegistry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pref = remember { PreferencesManager(ctx) }
    val currentService by pref.serviceFlow.collectAsState(initial = null)
    val services = MusicServiceRegistry.availableServices(ctx)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Version", style = MaterialTheme.typography.titleMedium)
            Button(onClick = {
                nav.navigate("selectVersion")
            }) {
                Text("change")
            }
            Text("Preferred service", style = MaterialTheme.typography.titleMedium)
            services.forEach { srv ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = srv.name == currentService, onClick = {
                            scope.launch { pref.setService(srv.name) }
                        })
                    Text("${srv.name}${if (!srv.recommended) " (not recommended – screen flashes)" else ""}")
                }
            }
        }
    }
}
