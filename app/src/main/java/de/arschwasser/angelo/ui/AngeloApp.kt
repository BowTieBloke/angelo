package de.arschwasser.angelo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.arschwasser.angelo.ui.screens.HomeScreen
import de.arschwasser.angelo.ui.screens.SelectVersionScreen
import de.arschwasser.angelo.ui.screens.SettingsScreen

@Composable
fun AngeloApp() {
    val nav = rememberNavController()
    MaterialTheme {
        NavHost(navController = nav, startDestination = "home") {
            composable("home") { HomeScreen(nav) }
            composable("selectVersion") { SelectVersionScreen(nav) }
            composable("settings") { SettingsScreen(nav) }
        }
    }
}
