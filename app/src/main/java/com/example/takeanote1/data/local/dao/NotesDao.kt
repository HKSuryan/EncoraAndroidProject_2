package com.example.takeanote1.data.local.dao

import android.provider.ContactsContract
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
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

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Query("""
        UPDATE notes 
        SET title = :title, content = :content, topic = :topic, reminderTime = :reminderTime 
        WHERE id = :noteId
    """)
    suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        topic: String,
        reminderTime: Long?
    )
    @Query("""
SELECT * FROM notes 
WHERE userId = :uid
  AND (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%')
  AND (:topic IS NULL OR topic = :topic)
  AND (:startDate IS NULL OR createdAt >= :startDate)
  AND (:endDate IS NULL OR createdAt <= :endDate)
  AND isCompleted = :isCompleted
ORDER BY
  CASE WHEN :sortField = 'createdAt' AND :sortOrder = 'ASC' THEN createdAt END ASC,
  CASE WHEN :sortField = 'createdAt' AND :sortOrder = 'DESC' THEN createdAt END DESC,
  CASE WHEN :sortField = 'title' AND :sortOrder = 'ASC' THEN title END ASC,
  CASE WHEN :sortField = 'title' AND :sortOrder = 'DESC' THEN title END DESC
""")
    fun getNotesPaged(
        uid: String,
        searchQuery: String?,    // make nullable
        sortField: String,
        sortOrder: String,
        topic: String?,
        isCompleted: Boolean,
        startDate: Long?,        // already nullable
        endDate: Long?           // already nullable
    ): PagingSource<Int, NoteEntity>






    @RawQuery(observedEntities = [NoteEntity::class])
    fun getNotesPaged(query: SupportSQLiteQuery): PagingSource<Int, NoteEntity>
}
