package com.example.meteopipli.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_THEME_KEY] ?: false }

    val areNotificationsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[NOTIFICATIONS_ENABLED_KEY] ?: true }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = isDark
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
}