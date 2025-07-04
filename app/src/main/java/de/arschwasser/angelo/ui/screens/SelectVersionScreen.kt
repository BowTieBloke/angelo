package de.arschwasser.angelo.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import de.arschwasser.angelo.core.AppConfig
import de.arschwasser.angelo.core.CSVImporter
import de.arschwasser.angelo.core.Downloader
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.core.SongDatabase
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectVersionScreen(nav: NavHostController) {
    val view = LocalView.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val pref = PreferencesManager(ctx)

    fun getFilenameFromUri(uri: Uri): String {
        val cursor = ctx.contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
        val name = if (cursor != null && cursor.moveToFirst() && nameIndex != -1) {
            cursor.getString(nameIndex)
        } else {
            uri.lastPathSegment ?: "import.csv"
        }
        cursor?.close()
        return name
    }

    val filePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val bytes =
                        ctx.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                    val filename = getFilenameFromUri(uri)
                    CSVImporter.import(ctx, bytes, filename)
                    pref.setGameVersion(filename)
                    nav.popBackStack()
                }
            }
        }

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
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select game version", style = MaterialTheme.typography.titleLarge)
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    scope.launch {
                        val url =
                            "https://docs.google.com/spreadsheets/d/e/2PACX-1vTyp8b7UmapJwCy033FMglAABejMpNQB0ezt2dCTR-CSh1WqLB3L0xjTkA1zr6_pEyDnmbIkS9X40CC/pub?gid=0&single=true&output=csv"
                        // previous server import:
                        // val json = Downloader.getString(AppConfig.BASE_URL + AppConfig.CSV_LIST_PATH)
                        // val arr = JSONObject(json).getJSONArray("versions")
                        // val obj = arr.getJSONObject(0)
                        // val oldUrl = "${'$'}{AppConfig.BASE_URL}/csv/${'$'}{obj.getString("filename")}"
                        val bytes = Downloader.getBytes(url)
                        CSVImporter.import(ctx, bytes, "server.csv")
                        pref.setGameVersion("server.csv")
                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        nav.popBackStack()
                    }
                },
                enabled = true
            ) { Text("Server") }

            Button(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                filePicker.launch("text/*")
            }) { Text("Custom") }

            Button(onClick = {
                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                scope.launch {
                    SongDatabase.get(ctx).songDao().clear()
                    pref.resetGameVersion()
                    nav.popBackStack()
                }
            }) { Text("Clear") }
        }
    }
}
