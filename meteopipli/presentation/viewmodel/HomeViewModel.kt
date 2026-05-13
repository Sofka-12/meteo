package com.example.meteopipli.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.meteopipli.data.local.MeteoDatabase
import com.example.meteopipli.data.repository.DiaryRepository
import com.example.meteopipli.data.repository.UserProfileRepository
import com.example.meteopipli.data.repository.WeatherRepository
import com.example.meteopipli.domain.ml.LogisticRegression
import com.example.meteopipli.domain.model.RiskLevel
import com.example.meteopipli.domain.model.UserProfile
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository()
    private val diaryRepository = DiaryRepository(MeteoDatabase.getDatabase(application))
    private val profileRepository = UserProfileRepository(application.applicationContext)
    private val mlModel = LogisticRegression(learningRate = 0.01, iterations = 500)

    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Состояния
    private val _riskLevel = MutableStateFlow(RiskLevel.GREEN)
    val riskLevel: StateFlow<RiskLevel> = _riskLevel

    private val _riskReason = MutableStateFlow("Загрузка данных...")
    val riskReason: StateFlow<String> = _riskReason

    private val _temperature = MutableStateFlow(0.0)
    val temperature: StateFlow<Double> = _temperature

    private val _pressure = MutableStateFlow(0.0)
    val pressure: StateFlow<Double> = _pressure

    private val _humidity = MutableStateFlow(0)
    val humidity: StateFlow<Int> = _humidity

    private val _kpIndex = MutableStateFlow(0)
    val kpIndex: StateFlow<Int> = _kpIndex

    // ML
    private val _personalRisk = MutableStateFlow(0.0)
    val personalRisk: StateFlow<Double> = _personalRisk

    // Данные для графиков
    private val _pressureChartData = MutableStateFlow<List<Double>>(emptyList())
    val pressureChartData: StateFlow<List<Double>> = _pressureChartData

    private val _temperatureChartData = MutableStateFlow<List<Double>>(emptyList())
    val temperatureChartData: StateFlow<List<Double>> = _temperatureChartData

    // Профиль пользователя для персонализации
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    init {
        loadProfile()
        loadModel()
        trainModelFromHistory()
        loadWeather()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            profileRepository.userProfileFlow.collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    fun loadWeather() {
        viewModelScope.launch {
            try {
                val location = getCurrentLocation()
                val lat = location?.latitude ?: 55.7558
                val lon = location?.longitude ?: 37.6176

                val weather = repository.getWeather(lat, lon)
                val kp = repository.getCurrentKpIndex()

                val currentTemp = weather.hourly.temperature_2m.firstOrNull() ?: 0.0
                val currentPressureHpa = weather.hourly.surface_pressure.firstOrNull() ?: 0.0
                val currentPressureMmHg = currentPressureHpa * 0.75006
                val currentHumidity = weather.hourly.relative_humidity_2m.firstOrNull() ?: 0

                _temperature.value = currentTemp
                _pressure.value = currentPressureMmHg
                _humidity.value = currentHumidity
                _kpIndex.value = kp

                // Данные для графиков
                val pressures = weather.hourly.surface_pressure.take(12).map { it * 0.75006 }
                val temps = weather.hourly.temperature_2m.take(12)
                _pressureChartData.value = pressures
                _temperatureChartData.value = temps

                // ML риск
                predictCurrentRisk()

                // Оценка риска (балльная система)
                val pressureChange = if (weather.hourly.surface_pressure.size >= 2) {
                    kotlin.math.abs(weather.hourly.surface_pressure[1] - weather.hourly.surface_pressure[0]) * 0.75006
                } else 0.0
                val tempChange = if (weather.hourly.temperature_2m.size >= 2) {
                    kotlin.math.abs(weather.hourly.temperature_2m[1] - weather.hourly.temperature_2m[0])
                } else 0.0

                var score = 0
                if (pressureChange > 5) score += 1
                if (pressureChange > 8) score += 1
                if (kp >= 5) score += 1
                if (kp >= 7) score += 1
                if (tempChange > 7) score += 1
                if (tempChange > 10) score += 1
                if (currentHumidity > 75) score += 1

                val risk = when {
                    score >= 3 -> RiskLevel.RED
                    score in 1..2 -> RiskLevel.YELLOW
                    else -> RiskLevel.GREEN
                }

                val reason = buildString {
                    if (pressureChange > 5) append("Скачок давления ${pressureChange.toInt()} мм. ")
                    if (kp >= 5) append("Магнитная буря (Kp=$kp). ")
                    if (tempChange > 7) append("Перепад температуры ${tempChange.toInt()}°C. ")
                    if (currentHumidity > 75) append("Высокая влажность $currentHumidity%. ")
                    if (isEmpty()) append("Показатели в пределах нормы")
                }

                _riskLevel.value = risk
                _riskReason.value = reason

            } catch (e: Exception) {
                _riskReason.value = "Ошибка: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun trainModelFromHistory() {
        viewModelScope.launch {
            diaryRepository.getAllEntries().collect { entries ->
                if (entries.size >= 10) {
                    val features = mutableListOf<DoubleArray>()
                    val labels = mutableListOf<Int>()
                    entries.forEach { entry ->
                        val feature = doubleArrayOf(
                            entry.temperature,
                            entry.pressure,
                            entry.humidity.toDouble(),
                            entry.kpIndex.toDouble()
                        )
                        features.add(feature)
                        labels.add(if (entry.feelingScore <= 2) 1 else 0)
                    }
                    mlModel.train(features, labels)
                    saveModel()
                    predictCurrentRisk()
                }
            }
        }
    }

    private fun saveModel() {
        val prefs = context.getSharedPreferences("ml_model", Context.MODE_PRIVATE)
        prefs.edit().putString("weights", mlModel.getWeights().joinToString(","))
            .putFloat("bias", mlModel.getBias().toFloat()).apply()
    }

    private fun loadModel() {
        val prefs = context.getSharedPreferences("ml_model", Context.MODE_PRIVATE)
        val weightsStr = prefs.getString("weights", null)
        val bias = prefs.getFloat("bias", 0f)
        if (weightsStr != null) {
            try {
                val weights = weightsStr.split(",").map { it.toDouble() }.toDoubleArray()
                mlModel.setWeightsAndBias(weights, bias.toDouble())
            } catch (e: Exception) { }
        }
    }

    private fun predictCurrentRisk() {
        if (mlModel.getWeights().isEmpty()) return
        val features = doubleArrayOf(
            _temperature.value,
            _pressure.value,
            _humidity.value.toDouble(),
            _kpIndex.value.toDouble()
        )
        val prob = mlModel.predict(features)
        _personalRisk.value = prob
    }

    private suspend fun getCurrentLocation(): android.location.Location? {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }
}