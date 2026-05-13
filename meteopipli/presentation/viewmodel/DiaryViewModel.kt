package com.example.meteopipli.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteopipli.data.local.MeteoDatabase
import com.example.meteopipli.data.repository.DiaryRepository
import com.example.meteopipli.domain.model.DiaryEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DiaryRepository(MeteoDatabase.getDatabase(application))
    private val _entries = MutableStateFlow<List<DiaryEntryEntity>>(emptyList())
    val entries: StateFlow<List<DiaryEntryEntity>> = _entries

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { list ->
                _entries.value = list
            }
        }
    }

    // В DiaryViewModel.kt
    fun addEntry(
        feeling: Int,
        symptoms: List<String>,
        note: String,
        temperature: Double,
        pressure: Double,
        humidity: Int,
        kpIndex: Int
    ) {
        viewModelScope.launch {
            val entry = DiaryEntryEntity(
                timestamp = System.currentTimeMillis(),
                feelingScore = feeling,
                symptoms = symptoms.joinToString(","),
                note = note,
                temperature = temperature,
                pressure = pressure,
                humidity = humidity,
                kpIndex = kpIndex
            )
            repository.insertEntry(entry)
        }
    }

    fun deleteEntry(entry: DiaryEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
}