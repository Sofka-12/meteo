package com.example.meteopipli.domain.model

data class DiaryEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long,
    val feelingScore: Int,   // 1–5
    val symptoms: List<String>,  // "headache", "fatigue", "joint_pain" и т.д.
    val note: String = ""
)