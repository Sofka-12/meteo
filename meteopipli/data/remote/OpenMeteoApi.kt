package com.example.meteopipli.data.remote

import com.example.meteopipli.domain.model.MagneticResponse
import com.example.meteopipli.domain.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,surface_pressure,wind_speed_10m",
        @Query("timezone") timezone: String = "Europe/Moscow"
    ): WeatherResponse

    @GET("v1/magnetic-storm")
    suspend fun getMagneticStorm(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("timezone") timezone: String = "Europe/Moscow"
    ): MagneticResponse
}