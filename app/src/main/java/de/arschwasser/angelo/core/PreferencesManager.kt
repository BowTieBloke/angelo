package de.arschwasser.angelo.core

import android.content.Context
import androidx.datastore.preferences.core.edit
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
}
