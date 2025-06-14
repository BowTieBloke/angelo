package de.arschwasser.angelo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.arschwasser.angelo.ui.screens.PermissionedHomeScreen
import de.arschwasser.angelo.ui.screens.SelectVersionScreen
import de.arschwasser.angelo.ui.screens.SettingsScreen
import de.arschwasser.angelo.ui.theme.AngeloTheme

@Composable
fun AngeloApp() {
    val nav = rememberNavController()
    AngeloTheme { // <-- Use your own theme here!
        NavHost(navController = nav, startDestination = "home") {
            composable("home") { PermissionedHomeScreen(nav) }
            composable("selectVersion") { SelectVersionScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
        }
    }
}
