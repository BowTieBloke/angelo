package de.arschwasser.angelo.core

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.arschwasser.angelo.model.Song

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Query("DELETE FROM songs")
    suspend fun clear()

    @Query("SELECT * FROM songs WHERE code = :code LIMIT 1")
    suspend fun findByCode(code: String): Song?

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}
