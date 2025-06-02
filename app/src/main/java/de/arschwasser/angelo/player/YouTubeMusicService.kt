package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.arschwasser.angelo.model.Song

class YouTubeMusicService : MusicService {
    override val name = "YouTube Music"
    override val recommended = true
    override fun isAvailable(context: Context) =
        context.packageManager.getLaunchIntentForPackage("com.google.android.apps.youtube.music") != null

    override suspend fun play(context: Context, song: Song): Boolean {
        val url = song.youtube ?: return false
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.apps.youtube.music")
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
        return true
    }
}
