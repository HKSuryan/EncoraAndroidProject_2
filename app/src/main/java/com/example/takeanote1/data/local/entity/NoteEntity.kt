package com.example.takeanote1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val topic: String, // predefined topic/tag
    val isCompleted: Boolean,
    val createdAt: Long,
    val reminderTime: Long? = null // For current day notes reminder
)
