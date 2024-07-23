package com.example.projectbangkit1.data.offline

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectbangkit1.data.response.ListStoryItem

@Database(
    entities = [ListStoryItem::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeys(): RemoteDao

    companion object {
        @Volatile
        private var instance: StoryDatabase? = null

        fun getDatabase(context: Context): StoryDatabase {
            if (instance == null) {
                synchronized(this) {
                    instance = getInstance(context)
                }
            }
            return instance!!
        }

        fun getInstance(context: Context): StoryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "Story.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}