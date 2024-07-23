package com.example.projectbangkit1

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.projectbangkit1.data.response.LoginResponse
import com.example.projectbangkit1.data.response.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingPreferences private constructor(private val dataStore: DataStore<Preferences>) {

    fun getUserData(): Flow<UserResponse> {
        return dataStore.data.map { preferences ->
            UserResponse(preferences[TOKEN] ?: "",
            preferences[STATUS] ?: false,
            preferences[NAMA] ?: "",
            preferences[USERID] ?: "")
        }
    }

    suspend fun putUserData(user : LoginResponse?) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = user?.loginResult?.token.toString()
            preferences[STATUS] = user?.error as Boolean
            preferences[NAMA] = user.loginResult?.name.toString()
            preferences[USERID] = user.loginResult?.userId.toString()
        }
    }

    suspend fun deleteData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("tokenuser")
        private val STATUS = booleanPreferencesKey("statususer")
        private val NAMA = stringPreferencesKey("namauser")
        private val USERID = stringPreferencesKey("userid")
        @Volatile
        private var INSTANCE: SettingPreferences? = null

        fun getInstance(dataStore: DataStore<Preferences>): SettingPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}