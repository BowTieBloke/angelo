package de.arschwasser.angelo.core

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import de.arschwasser.angelo.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CSVImporter {
    suspend fun import(context: Context, bytes: ByteArray, fileName: String) {
        val dao = SongDatabase.get(context).songDao()
        val songs = withContext(Dispatchers.IO) {
            csvReader().readAllWithHeader(bytes.inputStream()).map { row ->
                Song(
                    title = row["Title"] ?: "",
                    artist = row["artist"] ?: "",
                    year = row["year"]?.toIntOrNull() ?: 0,
                    code = row["code"] ?: "",
                    spotify = row["Spotify"],
                    youtube = row["YouTube"],
                    edition = fileName
                )
            }
        }
        dao.clear()
        dao.insertAll(songs)
    }
}
