package com.example.meteopipli.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meteopipli.domain.model.DiaryEntryEntity

@Database(
    entities = [DiaryEntryEntity::class],
    version = 2,        // Увеличиваем версию (было 1)
    exportSchema = false
)
abstract class MeteoDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile
        private var INSTANCE: MeteoDatabase? = null
        fun getDatabase(context: Context): MeteoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeteoDatabase::class.java,
                    "meteo_database"
                )
                    // Для разработки – позволяем миграцию с удалением данных
                    // В релизе нужно написать миграцию, но для теста сойдёт
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}