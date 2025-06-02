package de.arschwasser.angelo.player

import android.content.Context
import de.arschwasser.angelo.model.Song

interface MusicService {
    val name: String
    val recommended: Boolean
    fun isAvailable(context: Context): Boolean
    suspend fun play(context: Context, song: Song): Boolean
}
