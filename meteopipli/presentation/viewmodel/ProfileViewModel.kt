package com.example.meteopipli.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteopipli.data.repository.UserProfileRepository
import com.example.meteopipli.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserProfileRepository(application.applicationContext)

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                _profile.value = profile
                _isLoading.value = false
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile.copy(isFilled = true))
            loadProfile() // обновляем состояние
        }
    }
}