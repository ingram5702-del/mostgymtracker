package com.mostgymapp.app.timer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestTimerStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val endAtKey = longPreferencesKey("rest_timer_end_at")

    fun observeEndAtMs(): Flow<Long?> = dataStore.data.map { prefs -> prefs[endAtKey] }

    suspend fun saveEndAtMs(endAt: Long) {
        dataStore.edit { prefs ->
            prefs[endAtKey] = endAt
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(endAtKey)
        }
    }
}
