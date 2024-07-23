package com.example.projectbangkit1.ui.viewmodel

import androidx.lifecycle.*
import com.example.projectbangkit1.SettingPreferences
import com.example.projectbangkit1.data.response.LoginResponse
import com.example.projectbangkit1.data.response.UserResponse
import kotlinx.coroutines.launch

class SettingViewModel (private val pref: SettingPreferences) : ViewModel() {
    private val mlatlng = MutableLiveData<List<Float?>>()
    val latlng: LiveData<List<Float?>> = mlatlng


    fun setLatLng(lat: Float?, lon: Float?) {
        mlatlng.value = listOf(lat, lon)
    }

    fun getThemeSettings(): LiveData<UserResponse> {
        return pref.getUserData().asLiveData()
    }

    fun saveThemeSetting(userData: LoginResponse?) {
        viewModelScope.launch {
            pref.putUserData(userData)
        }
    }
    fun deleteData() {
        viewModelScope.launch {
            pref.deleteData()
        }
    }
}

class SettingFactory (private val pref: SettingPreferences) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}