package com.moduxi.monexi.data.repository.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

class OnboardingPrefs(private val context: Context) {
    private fun key(uid: String) = booleanPreferencesKey("onboarding_pending_$uid")

    fun isPending(uid: String): Flow<Boolean> = context.onboardingDataStore.data.map { preferences ->
        preferences[key(uid)] ?: false
    }

    suspend fun setPending(uid: String, value: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[key(uid)] = value
        }
    }
}
