package com.example.takeanote1.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        private const val TAG = "UserPreferences"
    }

    // Save userId
    suspend fun saveUserId(uid: String) {
        Log.d(TAG, "saveUserId: Saving uid: $uid")
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = uid
        }
        Log.d(TAG, "saveUserId: Successfully saved uid: $uid")
    }

    // Observe userId
    val userIdFlow: Flow<String?> =
        context.dataStore.data.map { preferences ->
            val uid = preferences[USER_ID]
            Log.d(TAG, "userIdFlow: Current uid in DataStore: $uid")
            uid
        }
}
