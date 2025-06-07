package de.arschwasser.angelo.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
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
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                }
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
            Button(onClick = {
                scope.launch {
                    val json = Downloader.getString(AppConfig.BASE_URL + AppConfig.CSV_LIST_PATH)
                    val arr = JSONObject(json).getJSONArray("versions")
                    if (arr.length() > 0) {
                        val obj = arr.getJSONObject(0) // first entry for simplicity
                        val bytes =
                            Downloader.getBytes("${AppConfig.BASE_URL}/csv/${obj.getString("filename")}")
                        val filename = obj.getString("filename")
                        CSVImporter.import(ctx, bytes, filename)
                        pref.setGameVersion(filename)
                        nav.popBackStack()
                    }
                }
            }) { Text("Server") }

            Button(onClick = { filePicker.launch("text/*") }) { Text("Custom") }

            Button(onClick = {
                scope.launch {
                    SongDatabase.get(ctx).songDao().clear()
                    pref.resetGameVersion()
                    nav.popBackStack()
                }
            }) { Text("Clear") }
        }
    }
}
