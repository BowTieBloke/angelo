package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import de.arschwasser.angelo.model.Song
import androidx.core.net.toUri

class SpotifyFreeService : MusicService {
    override val name = "Spotify Free (flash)"
    override val recommended = false
    override fun isAvailable(context: Context) =
        context.packageManager.getLaunchIntentForPackage("com.spotify.music") != null

    override suspend fun play(context: Context, song: Song): Boolean {
        val uri = song.spotify ?: return false
        val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
        return true
    }
}
