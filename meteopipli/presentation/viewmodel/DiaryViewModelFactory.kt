package com.example.meteopipli.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DiaryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}