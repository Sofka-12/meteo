package com.example.meteopipli.data.repository

import com.example.meteopipli.data.remote.NoaaRetrofitClient
import com.example.meteopipli.data.remote.RetrofitClient
import com.example.meteopipli.domain.model.MagneticResponse
import com.example.meteopipli.domain.model.WeatherResponse

class WeatherRepository {

    suspend fun getWeather(lat: Double = 55.7558, lon: Double = 37.6176): WeatherResponse {
        return RetrofitClient.api.getWeather(lat, lon)
    }

    // Устаревший метод (Open-Meteo не работает), оставлен для совместимости
    @Deprecated("Open-Meteo magnetic storm API is broken, use getCurrentKpIndex instead")
    suspend fun getMagneticStorm(lat: Double = 55.7558, lon: Double = 37.6176): MagneticResponse {
        return RetrofitClient.api.getMagneticStorm(lat, lon)
    }

    // Новый метод получения Kp индекса через NOAA с логированием и временной заглушкой
    suspend fun getCurrentKpIndex(): Int {
        return try {
            val response = NoaaRetrofitClient.api.getKpIndex()
            android.util.Log.d("MeteoApp", "NOAA response size: ${response.size}")

            if (response.size > 1) {
                val lastEntry = response.last()
                android.util.Log.d("MeteoApp", "Last entry: $lastEntry")
                // Пытаемся взять значение Kp из колонки 7 (последний 3-часовой интервал)
                // Если её нет – пробуем колонку 1
                val kpStr = lastEntry.getOrNull(7) ?: lastEntry.getOrNull(1) ?: "0"
                val kp = kpStr.toIntOrNull() ?: 0
                android.util.Log.d("MeteoApp", "Parsed Kp index: $kp")
                kp
            } else {
                android.util.Log.d("MeteoApp", "NOAA response too small, using fallback")
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("MeteoApp", "Error fetching Kp index, using random fallback", e)
            // ВРЕМЕННАЯ ЗАГЛУШКА: random от 2 до 7, чтобы увидеть работу интерфейса
            // Потом замените на 0, когда NOAA заработает
            (2..7).random()
        }
    }
}