package com.example.projectbangkit1.data.offline

import androidx.paging.PagingSource
import androidx.room.*
import com.example.projectbangkit1.data.response.ListStoryItem

@Dao
interface StoryDao {
    @Query("SELECT * FROM story")
    fun getStory(): PagingSource<Int, ListStoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(user: List<ListStoryItem>)

    @Query("DELETE FROM story")
    suspend fun deleteAll()

    @Query("SELECT * FROM story")
    fun getStoryWidget(): List<ListStoryItem>
}