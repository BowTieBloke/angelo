package de.arschwasser.angelo.player
/*

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.SpotifyAppRemote
import de.arschwasser.angelo.core.AppConfig
import de.arschwasser.angelo.model.Song
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SpotifyPremiumService : MusicService {
    override val name = "Spotify Premium"
    override val recommended = true
    override fun isAvailable(context: Context) =
        com.spotify.android.appremote.api.SpotifyAppRemote.isSpotifyInstalled(context)

    override suspend fun play(context: Context, song: Song): Boolean =
        suspendCancellableCoroutine { cont ->
            val uri = song.spotify ?: return@suspendCancellableCoroutine cont.resume(false)
            val params = ConnectionParams.Builder(AppConfig.SPOTIFY_CLIENT_ID)
                .setRedirectUri(AppConfig.SPOTIFY_REDIRECT_URI)
                .showAuthView(true)
                .build()
            SpotifyAppRemote.connect(context, params, object : SpotifyAppRemote.ConnectionListener {
                override fun onConnected(remote: SpotifyAppRemote) {
                    remote.playerApi.play(uri)
                    cont.resume(true)
                }
                override fun onFailure(throwable: Throwable) {
                    cont.resume(false)
                }
            })
        }
}
*/
