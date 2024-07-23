package com.example.projectbangkit1.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.projectbangkit1.data.offline.RemoteKeys
import com.example.projectbangkit1.data.offline.StoryDatabase
import com.example.projectbangkit1.data.online.ApiService
import com.example.projectbangkit1.data.response.ListStoryItem

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase, private val apiService: ApiService, private val token: String
): RemoteMediator<Int, ListStoryItem>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH ->{
                val remoteKeys = getRemoteKeyClosest(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val responseData = apiService.getStories("Bearer $token", page, state.config.pageSize).listStory
            val endOfPaginationReached = responseData.isEmpty()

            database.apply {
                withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        remoteKeys().deleteAll()
                        storyDao().deleteAll()
                    }
                    val prevKey = if (page == 1) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = responseData.map {
                        RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    remoteKeys().insertAll(keys)
                    storyDao().insertStory(responseData)
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getRemoteKeyLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let {
            database.remoteKeys().getAll(it.id)
        }
    }

    private suspend fun getRemoteKeyFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull{it.data.isNotEmpty()}?.data?.firstOrNull()?.let {
            database.remoteKeys().getAll(it.id)
        }
    }

    private suspend fun getRemoteKeyClosest(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let {
            state.closestItemToPosition(it)?.id?.let { data ->
                database.remoteKeys().getAll(data)
            }
        }
    }

    companion object {
        const val INITIAL_PAGE = 1
    }
}
