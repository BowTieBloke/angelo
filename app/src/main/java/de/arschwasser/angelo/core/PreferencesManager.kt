package de.arschwasser.angelo.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object PrefKeys {
    val SERVICE = stringPreferencesKey("service")
    val GAME_VERSION = stringPreferencesKey("game_version")
}

class PreferencesManager(private val context: Context) {
    val gameVersionDefault = "Not selected"
    val serviceFlow: Flow<String?> = context.dataStore.data.map { it[PrefKeys.SERVICE] }
    val gameVersionFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PrefKeys.GAME_VERSION] ?: gameVersionDefault }

    suspend fun setService(service: String) {
        context.dataStore.edit { it[PrefKeys.SERVICE] = service }
    }

    suspend fun setGameVersion(version: String) {
        context.dataStore.edit { it[PrefKeys.GAME_VERSION] = version }
    }

    suspend fun resetGameVersion() {
        context.dataStore.edit { it[PrefKeys.GAME_VERSION] = gameVersionDefault }
    }

    companion object {
        private val SERVICE_DELAY = longPreferencesKey("service_delay")
        private const val DEFAULT_SERVICE_DELAY = 500L
    }

    val serviceDelayFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_DELAY] ?: DEFAULT_SERVICE_DELAY
        }

    suspend fun setServiceDelay(delay: Long) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_DELAY] = delay
        }
    }

    suspend fun resetServiceDelay() {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_DELAY] = DEFAULT_SERVICE_DELAY
        }
    }

    val serviceDelayDefault = DEFAULT_SERVICE_DELAY
}
