package de.arschwasser.angelo.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.arschwasser.angelo.model.Song

@Database(entities = [Song::class], version = 2, exportSchema = false)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: SongDatabase? = null

        // Define your migrations here
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new table with the new schema (no 'amazon', with 'edition')
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `songs_new` (
                `title` TEXT NOT NULL,
                `artist` TEXT NOT NULL,
                `year` INTEGER NOT NULL,
                `code` TEXT NOT NULL,
                `spotify` TEXT,
                `youtube` TEXT,
                `edition` TEXT,
                PRIMARY KEY(`code`)
            )
        """.trimIndent()
                )
                // 2. Copy the old data (amazon will be dropped, edition will be NULL)
                db.execSQL(
                    """
            INSERT INTO `songs_new` (`title`, `artist`, `year`, `code`, `spotify`, `youtube`)
            SELECT `title`, `artist`, `year`, `code`, `spotify`, `youtube`
            FROM `songs`
        """.trimIndent()
                )
                // 3. Drop old table
                db.execSQL("DROP TABLE `songs`")
                // 4. Rename new table to original name
                db.execSQL("ALTER TABLE `songs_new` RENAME TO `songs`")
            }
        }

        fun get(context: Context): SongDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java,
                    "angelo.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
    }
}
