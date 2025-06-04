package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import de.arschwasser.angelo.model.Song
import androidx.core.net.toUri

class YouTubeMusicService : MusicService {
    override val name = "YouTube Music"
    override val recommended = true
    override fun isAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.apps.youtube.music", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun play(context: Context, song: Song): Boolean {
        val url = song.youtube ?: return false
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setPackage("com.google.android.apps.youtube.music")
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: android.content.ActivityNotFoundException) {
            false
        }
    }

}
