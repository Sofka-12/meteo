package com.example.meteopipli.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.meteopipli.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserProfileRepository(private val context: Context) {

    companion object {
        val IS_FILLED = stringPreferencesKey("is_filled")
        val AGE_GROUP = stringPreferencesKey("age_group")
        val CHRONIC_DISEASES = stringSetPreferencesKey("chronic_diseases")
        val OTHER_DISEASE = stringPreferencesKey("other_disease")
        val WEATHER_SENSITIVITY = stringPreferencesKey("weather_sensitivity")
        val SYMPTOMS = stringSetPreferencesKey("symptoms")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            isFilled = prefs[IS_FILLED]?.toBoolean() ?: false,
            ageGroup = prefs[AGE_GROUP] ?: "",
            chronicDiseases = prefs[CHRONIC_DISEASES]?.toList() ?: emptyList(),
            otherDisease = prefs[OTHER_DISEASE] ?: "",
            weatherSensitivity = prefs[WEATHER_SENSITIVITY] ?: "",
            symptoms = prefs[SYMPTOMS]?.toList() ?: emptyList()
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[IS_FILLED] = profile.isFilled.toString()
            prefs[AGE_GROUP] = profile.ageGroup
            prefs[CHRONIC_DISEASES] = profile.chronicDiseases.toSet()
            prefs[OTHER_DISEASE] = profile.otherDisease
            prefs[WEATHER_SENSITIVITY] = profile.weatherSensitivity
            prefs[SYMPTOMS] = profile.symptoms.toSet()
        }
    }

    suspend fun clearProfile() {
        context.dataStore.edit { it.clear() }
    }
}