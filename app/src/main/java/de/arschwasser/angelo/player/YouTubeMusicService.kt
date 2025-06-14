package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.core.ScreenCover
import de.arschwasser.angelo.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class YouTubeMusicService : MusicService {

    override val name = "YouTube Music"
    override val recommended = true

    override fun isAvailable(context: Context): Boolean = try {
        context.packageManager.getPackageInfo("com.google.android.apps.youtube.music", 0)
        true
    } catch (_: Exception) {
        false
    }

    override suspend fun play(context: Context, song: Song): Boolean {
        return try {
            // 1. Cover the screen and open the song in YouTube Music (on main thread)
            withContext(Dispatchers.Main) {
                ScreenCover.show(context)
                val ytMusicIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = song.youtube?.toUri()
                    setPackage("com.google.android.apps.youtube.music")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(ytMusicIntent)
            }

            // 2. Get the delay preference (suspending)
            val pref = PreferencesManager(context)
            val delayMs = pref.serviceDelayFlow.first()

            // 3. Wait for the delay (on background thread)
            delay(delayMs)

            // 4. Bring your app back to the foreground (on main thread)
            withContext(Dispatchers.Main) {
                val packageManager = context.packageManager
                val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    ScreenCover.hide()
                    true
                } else {
                    ScreenCover.hide()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
