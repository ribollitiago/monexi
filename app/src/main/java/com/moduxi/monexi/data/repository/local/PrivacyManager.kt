package com.moduxi.monexi.data.repository.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.privacyDataStore by preferencesDataStore(name = "privacy_prefs")

class PrivacyManager(private val context: Context) {

    private val IS_BALANCE_VISIBLE = booleanPreferencesKey("is_balance_visible")
    private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")

    val isBalanceVisible: Flow<Boolean> = context.privacyDataStore.data.map { preferences ->
        preferences[IS_BALANCE_VISIBLE] ?: true
    }

    val isBiometricEnabled: Flow<Boolean> = context.privacyDataStore.data.map { preferences ->
        preferences[IS_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBalanceVisible(visible: Boolean) {
        context.privacyDataStore.edit { preferences ->
            preferences[IS_BALANCE_VISIBLE] = visible
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.privacyDataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = enabled
        }
    }
}
