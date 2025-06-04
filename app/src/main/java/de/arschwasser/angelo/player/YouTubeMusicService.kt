package de.arschwasser.angelo.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.net.toUri
import de.arschwasser.angelo.model.Song
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class YouTubeMusicService : MusicService {

    override val name = "YouTube Music"
    override val recommended = true

    override fun isAvailable(context: Context): Boolean = try {
        context.packageManager.getPackageInfo("com.google.android.apps.youtube.music", 0)
        true
    } catch (_: Exception) {
        false
    }

    /**
     * Try the hidden MediaBrowser route first; if that fails,
     * fall back to the normal ACTION_VIEW intent.
     */
    override suspend fun play(context: Context, song: Song): Boolean {
        val uri = song.youtube?.toUri() ?: return false

        /* ---------- hidden playback (no UI) ---------- */
        val hiddenSucceeded = suspendCancellableCoroutine<Boolean> { cont ->
            val component = ComponentName(
                "com.google.android.apps.youtube.music",
                "com.google.android.apps.youtube.music.MediaPlaybackService"
            )

            // 1) declare the var first so the callback can see it
            lateinit var mediaBrowser: MediaBrowserCompat

            // 2) build the callback, referencing the var
            val callback = object : MediaBrowserCompat.ConnectionCallback() {

                override fun onConnected() {
                    runCatching {
                        val controller = MediaControllerCompat(
                            context,
                            mediaBrowser.sessionToken
                        )
                        controller.transportControls.playFromUri(uri, null)
                    }
                    mediaBrowser.disconnect()
                    if (cont.isActive) cont.resume(true)
                }

                override fun onConnectionSuspended() {
                    mediaBrowser.disconnect()
                    if (cont.isActive) cont.resume(false)
                }

                override fun onConnectionFailed() {
                    mediaBrowser.disconnect()
                    if (cont.isActive) cont.resume(false)
                }
            }

            // 3) instantiate the browser *after* the callback exists
            mediaBrowser = MediaBrowserCompat(context, component, callback, null)

            runCatching { mediaBrowser.connect() }
                .onFailure { if (cont.isActive) cont.resume(false) }

            cont.invokeOnCancellation { mediaBrowser.disconnect() }
        }

        if (hiddenSucceeded) return true

        /* ---------- visible fallback ---------- */
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.youtube.music")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }

        return runCatching { context.startActivity(intent) }.isSuccess
    }
}
