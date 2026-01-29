package com.example.takeanote1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int? = null, // Optional: link reminder to a note
    val title: String,
    val description: String = "",
    val reminderDateTime: Long, // timestamp in milliseconds
    val isCompleted: Boolean = false,
    val isNotified: Boolean = false,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)