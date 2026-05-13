package com.example.meteopipli.domain.model

data class WeatherData(
    val timestamp: Long,
    val temperature: Double,
    val pressure: Double,
    val humidity: Int,
    val windSpeed: Double
)