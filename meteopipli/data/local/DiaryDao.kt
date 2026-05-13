package com.example.meteopipli.data.local

import androidx.room.*
import com.example.meteopipli.domain.model.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntryEntity>>

    @Insert
    suspend fun insert(entry: DiaryEntryEntity)

    @Delete
    suspend fun delete(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAll()
}