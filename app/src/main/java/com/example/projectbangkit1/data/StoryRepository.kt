package com.example.projectbangkit1.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.paging.*
import com.example.projectbangkit1.data.offline.StoryDatabase
import com.example.projectbangkit1.data.online.ApiService
import com.example.projectbangkit1.data.response.APIResponse
import com.example.projectbangkit1.data.response.DetailResponse
import com.example.projectbangkit1.data.response.ListStoryItem
import com.example.projectbangkit1.data.response.LoginResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository private constructor(private val apiService: ApiService, private val storyDatabase: StoryDatabase){
    private val _user = MutableLiveData<LoginResponse?>()
    private val user: LiveData<LoginResponse?> = _user

    private val _response = MutableLiveData<APIResponse?>()
    private val response: LiveData<APIResponse?> = _response

    private val _stories = MutableLiveData<List<ListStoryItem?>?>()
    private val stories: LiveData<List<ListStoryItem?>?> = _stories

    private val _detail = MutableLiveData<DetailResponse>()
    private val detail: LiveData<DetailResponse> = _detail

    fun login(email: String, pass: String): LiveData<Result<LoginResponse?>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.postLogin(email, pass)
            _user.value = response

        } catch (e: Exception) {
            Log.d("StoryRepository", "${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<LoginResponse?>> = user.map { Result.Success(it) }
        emitSource(localData)
    }

    fun register(nama: String, email: String, pass: String): LiveData<Result<APIResponse?>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.postRegister(nama, email, pass)
            _response.value = response

        } catch (e: Exception) {
            Log.d("StoryRepository", "${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<APIResponse?>> = response.map { Result.Success(it) }
        emitSource(localData)
    }

    fun postStory(token: String, desc: String, getFile: File, lat: Float?, lon: Float?): LiveData<Result<APIResponse?>> = liveData {
        val description = desc.toRequestBody("text/plain".toMediaType())
        val requestImageFile = getFile.asRequestBody("image/jpeg".toMediaType())
        val imageMultiPart: MultipartBody.Part = MultipartBody.Part.createFormData("photo", getFile.name,requestImageFile)
        emit(Result.Loading)
        try {
            val response = apiService.postStory("Bearer $token", description, imageMultiPart, lat, lon)
            _response.value = response

        } catch (e: Exception) {
            Log.d("NewsRepository", "getHeadlineNews: ${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<APIResponse?>> = response.map { Result.Success(it) }
        emitSource(localData)
    }

    fun getMapStory(token: String): LiveData<Result<List<ListStoryItem?>?>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getMapStories("Bearer $token", DEFAULTSIZE, LOCATION)
            val story = response.listStory
            _stories.value = story
        } catch (e: Exception) {
            Log.d("StoryRepository", "${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<List<ListStoryItem?>?>> = stories.map { Result.Success(it) }
        emitSource(localData)
    }

    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = DEFAULTPAGE),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {storyDatabase.storyDao().getStory()}
        ).liveData
    }

    fun getDetail(token: String, id: String): LiveData<Result<DetailResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getDetail("Bearer $token", id)
            _detail.value = response

        } catch (e: Exception) {
            Log.d("StoryRepository", "${e.message.toString()} ")
            emit(Result.Error(e.message.toString()))
        }
        val localData: LiveData<Result<DetailResponse>> = detail.map { Result.Success(it) }
        emitSource(localData)
    }

    companion object {
        const val DEFAULTSIZE = 50
        const val DEFAULTPAGE = 20
        const val LOCATION = 1

        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, storyDatabase)
            }.also { instance = it }
    }
}