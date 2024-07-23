package com.example.projectbangkit1.ui.viewmodel

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectbangkit1.data.StoryRepository
import com.example.projectbangkit1.data.response.ListStoryItem
import com.example.projectbangkit1.di.Injection
import java.io.File

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val mTempFile = MutableLiveData<File>()
    val tempFile: LiveData<File> = mTempFile

    private val mRotate = MutableLiveData<Boolean>().apply { postValue(true) }
    val rotate: LiveData<Boolean> = mRotate

    fun login(email: String, pass: String) = storyRepository.login(email, pass)
    fun register(nama: String, email: String, pass: String) = storyRepository.register(nama, email, pass)
    fun postStory(token: String, desc: String, getFile: File, lat: Float?, lon: Float?) = storyRepository.postStory(token, desc, getFile, lat, lon)
    fun getStory(token: String): LiveData<PagingData<ListStoryItem>> = storyRepository.getStory(token).cachedIn(viewModelScope)
    fun getMapStory(token: String) = storyRepository.getMapStory(token)
    fun getDetail(token: String, id: String) = storyRepository.getDetail(token, id)
    fun setFile(file: File) {
        mTempFile.value = file
    }
    fun setRotate(state: Boolean) {
        mRotate.value = state
    }
}

class ViewModelFactory (private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(Injection.provideRepo(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}