package com.example.weatherapp.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object PreferencesManager {
    private const val PREFERENCES_NAME = "weather_prefs"
    private const val KEY_LAST_CITY = "key_last_city"

    suspend fun storeLastCity(context: Context, cityName: String) {
        context.preferencesDataStore.edit { prefs ->
            prefs[stringPreferencesKey(KEY_LAST_CITY)] = cityName
        }
    }

    suspend fun retrieveLastCity(context: Context): String? {
        val prefs = context.preferencesDataStore.data.first()
        return prefs[stringPreferencesKey(KEY_LAST_CITY)]
    }

    private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
}
