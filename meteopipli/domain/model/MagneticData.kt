package com.example.meteopipli.domain.model

data class MagneticData(
    val timestamp: Long,
    val kpIndex: Int,
    val description: String  // quiet / unsettled / storm
)