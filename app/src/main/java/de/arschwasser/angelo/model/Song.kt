package de.arschwasser.angelo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    val title: String,
    val artist: String,
    val year: Int,
    @PrimaryKey
    val code: String,
    val spotify: String?,
    val youtube: String?,
    val album: String?,
    val edition: String?
)
