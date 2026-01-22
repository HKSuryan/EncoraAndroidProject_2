package com.example.takeanote1.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
    }

    // Save userId
    suspend fun saveUserId(uid: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = uid
        }
    }

    // Observe userId
    val userIdFlow: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_ID]
        }
}
