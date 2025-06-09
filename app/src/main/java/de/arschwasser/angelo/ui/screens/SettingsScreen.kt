package de.arschwasser.angelo.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.player.MusicServiceRegistry
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController) {
    val view = LocalView.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pref = remember { PreferencesManager(ctx) }
    val currentService by pref.serviceFlow.collectAsState(initial = null)
    val delayPref by pref.serviceDelayFlow.collectAsState(initial = pref.serviceDelayDefault)
    val services = MusicServiceRegistry.availableServices(ctx)
    val gameVersion by pref.gameVersionFlow.collectAsState(initial = "Not selected")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Angelo") },
                actions = {
                    IconButton(onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                        nav.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Version", style = MaterialTheme.typography.titleMedium)
            Text(text = gameVersion)
            Button(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                nav.navigate("selectVersion")
            }) {
                Text("change")
            }
            Text("Preferred service", style = MaterialTheme.typography.titleMedium)
            services.forEach { srv ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = srv.name == currentService,
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            scope.launch { pref.setService(srv.name) }
                        }
                    )
                    Text("${srv.name}${if (!srv.recommended) " (not recommended – screen flashes)" else ""}")
                }
            }
            Text("Service delay", style = MaterialTheme.typography.titleMedium)

            // Use remember to prevent recomposition loop on Slider
            var sliderValue by remember { mutableFloatStateOf(delayPref.toFloat()) }
            // If the preference changes (e.g. by restore), update slider
            LaunchedEffect(delayPref) { sliderValue = delayPref.toFloat() }

            Button(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                scope.launch { pref.resetServiceDelay() }
            }) {
                Text("reset")
            }
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                },
                onValueChangeFinished = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    scope.launch { pref.setServiceDelay(sliderValue.toLong()) }
                },
                valueRange = 0f..3000f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(text = "${sliderValue.toLong()} ms")
        }
    }
}
