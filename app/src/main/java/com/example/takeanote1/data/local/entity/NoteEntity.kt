package com.example.takeanote1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
// This is how data is stored in the database
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val isCompleted: Boolean,
    val createdAt: Long
)
