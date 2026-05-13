package com.example.meteopipli.domain.model


enum class UserGroup {
    HYPOTONIC,
    HYPERTONIC,
    JOINT_DISEASE,
    MIGRAINE,
    ASTHMA,
    DEFAULT
}
data class UserProfile(
    val isFilled: Boolean = false,          // заполнена ли анкета
    val ageGroup: String = "",              // "до 30", "30-45", "46-60", "старше 60"
    val chronicDiseases: List<String> = emptyList(), // "гипертония", "гипотония", "болезнь суставов", "мигрень", "астма", "другое"
    val otherDisease: String = "",          // текст для "другое"
    val weatherSensitivity: String = "",    // "редко", "1-2 раза в месяц", "иногда", "раз в неделю", "2-3 раза в неделю", "всегда"
    val symptoms: List<String> = emptyList() // из списка симптомов дневника
)