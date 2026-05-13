package com.example.meteopipli.domain.model

data class MagneticResponse(
    val latitude: Double,
    val longitude: Double,
    val daily: MagneticDaily
)

data class MagneticDaily(
    val time: List<String>,
    val kp_index: List<Int>,
    val kp_description: List<String>
)