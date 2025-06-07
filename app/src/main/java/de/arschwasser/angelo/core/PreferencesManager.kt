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
}

class PreferencesManager(private val context: Context) {
    val serviceFlow: Flow<String?> = context.dataStore.data.map { it[PrefKeys.SERVICE] }

    suspend fun setService(service: String) {
        context.dataStore.edit { it[PrefKeys.SERVICE] = service }
    }

    companion object {
        private val SERVICE_DELAY = longPreferencesKey("service_delay")
        private const val DEFAULT_SERVICE_DELAY = 500L
    }

    // Flow to observe serviceDelay changes
    val serviceDelayFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[SERVICE_DELAY] ?: DEFAULT_SERVICE_DELAY
        }

    // Suspend function to update the delay
    suspend fun setServiceDelay(delay: Long) {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_DELAY] = delay
        }
    }

    // Suspend function to update the delay
    suspend fun resetServiceDelay() {
        context.dataStore.edit { preferences ->
            preferences[SERVICE_DELAY] = DEFAULT_SERVICE_DELAY
        }
    }

    val serviceDelayDefault = DEFAULT_SERVICE_DELAY;

}
