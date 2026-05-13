package com.example.meteopipli.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteopipli.utils.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsDataStore.isDarkThemeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val areNotificationsEnabled: StateFlow<Boolean> = settingsDataStore.areNotificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun toggleDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkTheme(isDark)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setNotificationsEnabled(enabled)
        }
    }
}

class SettingsViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(SettingsDataStore(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}