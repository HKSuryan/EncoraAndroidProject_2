package com.example.takeanote1.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.takeanote1.data.local.dao.NotesDao
import com.example.takeanote1.data.local.dao.ReminderDao
import com.example.takeanote1.data.local.dao.UserDao
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.local.entity.Reminder
import com.example.takeanote1.data.local.entity.UserEntity

@Database(entities = [NoteEntity::class, UserEntity::class, Reminder::class], version = 3, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notesDao(): NotesDao
    abstract fun userDao(): UserDao
    abstract fun reminderDao(): ReminderDao
}
