package com.example.meteopipli.data.repository

import com.example.meteopipli.data.local.MeteoDatabase
import com.example.meteopipli.domain.model.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val db: MeteoDatabase) {
    fun getAllEntries(): Flow<List<DiaryEntryEntity>> = db.diaryDao().getAllEntries()
    suspend fun insertEntry(entry: DiaryEntryEntity) = db.diaryDao().insert(entry)
    suspend fun deleteEntry(entry: DiaryEntryEntity) = db.diaryDao().delete(entry)
}