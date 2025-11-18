package com.secure.privacyfirst.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesManager(private val context: Context) {
    
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        private val USER_NAME = stringPreferencesKey("user_name")
    }
    
    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }
    
    val isSetupCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SETUP_COMPLETED] ?: false
    }
    
    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: ""
    }
    
    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }
    
    suspend fun setSetupCompleted(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETED] = true
            preferences[USER_NAME] = userName
        }
    }
}
