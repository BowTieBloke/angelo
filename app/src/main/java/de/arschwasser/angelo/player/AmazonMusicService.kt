package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import de.arschwasser.angelo.model.Song
import androidx.core.net.toUri

class AmazonMusicService : MusicService {
    override val name = "Amazon Music (flash)"
    override val recommended = false
    override fun isAvailable(context: Context) =
        context.packageManager.getLaunchIntentForPackage("com.amazon.mp3") != null

    override suspend fun play(context: Context, song: Song): Boolean {
        val url = song.amazon ?: return false
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            setPackage("com.amazon.mp3")
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
        return true
    }
}
