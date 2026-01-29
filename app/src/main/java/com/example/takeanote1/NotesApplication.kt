package com.example.takeanote1

import android.app.Application
import androidx.room.Room
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.database.NotesDatabase
import com.example.takeanote1.data.repository.NotesRepository
import com.example.takeanote1.data.repository.WorkManagerNotificationRepository

class NotesApplication : Application() {

    private val database by lazy {
        Room.databaseBuilder(this, NotesDatabase::class.java, "notes_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy {
        NotesRepository(
            database.notesDao(),
            database.userDao(),
            database.reminderDao()
        )
    }

    val userPreferences by lazy {
        UserPreferences(this)
    }

    val notificationRepository by lazy {
        WorkManagerNotificationRepository(this)
    }
}
