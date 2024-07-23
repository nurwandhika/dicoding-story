package com.example.projectbangkit1.di

import android.content.Context
import com.example.projectbangkit1.data.offline.StoryDatabase
import com.example.projectbangkit1.data.StoryRepository
import com.example.projectbangkit1.data.online.ApiConfig


object Injection {
    fun provideRepo(context: Context) : StoryRepository {
        val apiService = ApiConfig.getApiService()
        val database = StoryDatabase.getInstance(context)
        return StoryRepository.getInstance(apiService, database)
    }
}