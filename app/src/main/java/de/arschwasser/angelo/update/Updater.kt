package de.arschwasser.angelo.update

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.core.content.FileProvider
import de.arschwasser.angelo.core.AppConfig
import de.arschwasser.angelo.core.Downloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest


object Updater {
    class IntegrityException : IllegalStateException("Downloaded APK hash does not match update metadata")

    data class Info(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val changelog: String,
        val sha256: String?
    )

    suspend fun check(): Info? = withContext(Dispatchers.IO) {
        val json = Downloader.getString(AppConfig.BASE_URL + AppConfig.UPDATE_JSON)
        val obj = JSONObject(json)
        val vc = obj.getInt("versionCode")
        if (vc > de.arschwasser.angelo.BuildConfig.VERSION_CODE) {
            Info(
                versionCode = vc,
                versionName = obj.getString("versionName"),
                apkUrl = resolveUrl(obj.getString("apkUrl")),
                changelog = obj.optString("changelog", ""),
                sha256 = obj.optString("sha256").takeIf { it.isNotBlank() }
            )
        } else null
    }

    suspend fun download(context: Context, info: Info): File = withContext(Dispatchers.IO) {
        val bytes = Downloader.getBytes(info.apkUrl)
        if (info.sha256 != null && sha256(bytes) != normalizeHash(info.sha256)) {
            throw IntegrityException()
        }
        val file = File(context.cacheDir, "angelo-update.apk")
        file.writeBytes(bytes)
        file
    }

    fun canRequestPackageInstalls(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun resolveUrl(url: String): String =
        if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            AppConfig.BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
        }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun normalizeHash(hash: String): String =
        hash.trim().lowercase().removePrefix("sha256:").replace(" ", "")
}
