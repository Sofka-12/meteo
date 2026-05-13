package com.example.meteopipli.domain.model

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val hourly: Hourly
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>,
    val surface_pressure: List<Double>,
    val wind_speed_10m: List<Double>
)