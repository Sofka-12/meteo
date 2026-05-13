package com.example.meteopipli.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val feelingScore: Int,          // 1–5
    val symptoms: String,           // "headache,fatigue"
    val note: String = "",
    // Добавленные поля для ML
    val temperature: Double,        // °C
    val pressure: Double,           // мм рт. ст.
    val humidity: Int,              // %
    val kpIndex: Int                // Kp-индекс
)