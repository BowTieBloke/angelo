package de.arschwasser.angelo.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.arschwasser.angelo.ui.screens.HomeScreen
import de.arschwasser.angelo.ui.screens.SelectVersionScreen
import de.arschwasser.angelo.ui.screens.SettingsScreen
import de.arschwasser.angelo.ui.theme.AngeloTheme

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AngeloApp() {
    val nav = rememberNavController()
    AngeloTheme { // <-- Use your own theme here!
        NavHost(navController = nav, startDestination = "home") {
            composable("home") { HomeScreen(nav) }
            composable("selectVersion") { SelectVersionScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
        }
    }
}
