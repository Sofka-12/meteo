package com.example.meteopipli

import android.app.Application
import androidx.work.*
import com.example.meteopipli.workers.WeatherCheckWorker
import java.util.concurrent.TimeUnit

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleWeatherChecks()
    }

    private fun scheduleWeatherChecks() {
        // Ограничения: нужен интернет
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Периодическая работа (каждые 6 часов)
        val request = PeriodicWorkRequestBuilder<WeatherCheckWorker>(
            6, TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        // Запускаем, если ещё не запущена
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "weather_check",            // уникальное имя задачи
            ExistingPeriodicWorkPolicy.KEEP, // если уже есть, не создавать новую
            request
        )
    }
}