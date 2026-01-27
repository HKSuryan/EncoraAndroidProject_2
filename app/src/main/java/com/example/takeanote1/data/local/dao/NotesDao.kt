package com.example.takeanote1.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.takeanote1.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE userId = :uid ORDER BY createdAt DESC")
    fun getNotes(uid: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :uid AND isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedNotes(uid: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :uid AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveNotes(uid: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :uid AND reminderTime >= :startOfDay AND reminderTime <= :endOfDay")
    fun getTodayReminders(uid: String, startOfDay: Long, endOfDay: Long): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isCompleted = :isCompleted WHERE id = :noteId")
    suspend fun updateNoteCompletion(noteId: String, isCompleted: Boolean)

    @Query("DELETE FROM notes WHERE userId = :uid AND isCompleted = 1")
    suspend fun deleteCompletedNotes(uid: String)

    @Query("DELETE FROM notes WHERE userId = :uid AND id IN (:ids)")
    suspend fun deleteNotesByIds(uid: String, ids: List<String>)
}
