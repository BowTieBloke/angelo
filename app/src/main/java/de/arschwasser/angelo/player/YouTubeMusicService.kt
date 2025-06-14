package de.arschwasser.angelo.player

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import de.arschwasser.angelo.core.PreferencesManager
import de.arschwasser.angelo.core.ScreenCover
import de.arschwasser.angelo.model.Song
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun play(context: Context, song: Song): Boolean {
        return try {
            val coverView = ScreenCover.show(context)
            // Launch a coroutine to auto-hide after 10s (even if play logic hangs)
            val overlayTimeoutJob = kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                delay(10_000)
                ScreenCover.hide(context, coverView)
            }

            // 1. Cover the screen and open the song in YouTube Music (on main thread)
            withContext(Dispatchers.Main) {
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
                    ScreenCover.hide(context, coverView)
                    overlayTimeoutJob.cancel() // Cancel the auto-hide if not needed
                    true
                } else {
                    ScreenCover.hide(context, coverView)
                    overlayTimeoutJob.cancel()
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
