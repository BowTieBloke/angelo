package de.arschwasser.angelo.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import de.arschwasser.angelo.core.AppConfig
import de.arschwasser.angelo.core.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

object Updater {
    data class Info(val versionCode: Int, val versionName: String, val apkUrl: String, val changelog: String)
    suspend fun check(context: Context): Info? = withContext(Dispatchers.IO) {
        val json = Downloader.getString(AppConfig.BASE_URL + AppConfig.UPDATE_JSON)
        val obj = JSONObject(json)
        val vc = obj.getInt("versionCode")
        if (vc > de.arschwasser.angelo.BuildConfig.VERSION_CODE) {
            Info(vc, obj.getString("versionName"), obj.getString("apkUrl"), obj.optString("changelog", ""))
        } else null
    }

    suspend fun download(context: Context, url: String): File = withContext(Dispatchers.IO) {
        val bytes = Downloader.getBytes(url)
        val file = File(context.cacheDir, "angelo-update.apk")
        file.writeBytes(bytes)
        file
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
