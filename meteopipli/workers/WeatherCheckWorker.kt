package com.example.meteopipli.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.meteopipli.data.repository.WeatherRepository
import com.example.meteopipli.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = WeatherRepository()
    // Координаты по умолчанию (Москва) или можно получить последнюю локацию
    private val defaultLat = 55.7558
    private val defaultLon = 37.6176

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val weather = repository.getWeather(defaultLat, defaultLon)
            val kp = repository.getCurrentKpIndex() // используем ваш метод с NOAA (возможно заглушка)

            // Берём текущие показатели и проверяем пороги
            val currentPressureHpa = weather.hourly.surface_pressure.firstOrNull() ?: 0.0
            val currentPressureMmHg = currentPressureHpa * 0.75006
            val isLowPressure = currentPressureMmHg < 740
            val isHighPressure = currentPressureMmHg > 780
            val hasMagneticStorm = kp >= 5

            // Проверяем скачок давления за последние 6 часов (нужно вычислить)
            // Упрощённо: возьмём разницу между первым и вторым часом (для демонстрации)
            val pressureChange = if (weather.hourly.surface_pressure.size >= 2) {
                kotlin.math.abs(weather.hourly.surface_pressure[1] - weather.hourly.surface_pressure[0]) * 0.75006
            } else 0.0
            val isPressureSpike = pressureChange > 5.0

            if (isLowPressure || isHighPressure || isPressureSpike || hasMagneticStorm) {
                val title = "Метео-предупреждение"
                val message = buildString {
                    if (isLowPressure) append("Давление упало до ${currentPressureMmHg.toInt()} мм. ")
                    if (isHighPressure) append("Давление поднялось до ${currentPressureMmHg.toInt()} мм. ")
                    if (isPressureSpike) append("Резкий скачок давления! ")
                    if (hasMagneticStorm) append("Магнитная буря (Kp=$kp). ")
                    append("Примите меры заранее.")
                }
                NotificationHelper.sendAlertNotification(applicationContext, title, message)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}