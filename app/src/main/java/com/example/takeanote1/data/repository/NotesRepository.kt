package com.example.takeanote1.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.takeanote1.data.local.dao.NotesDao
import com.example.takeanote1.data.local.dao.ReminderDao
import com.example.takeanote1.data.local.dao.UserDao
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.local.entity.Reminder
import com.example.takeanote1.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

class NotesRepository(
    private val notesDao: NotesDao,
    private val userDao: UserDao,
    private val reminderDao: ReminderDao
) {
    private val TAG = "NotesRepository"

    // -------------------- User operations --------------------
    suspend fun saveUser(user: UserEntity) {
        Log.d(TAG, "saveUser: Attempting to save user with ID: ${user.id}")
        userDao.insertUser(user)
        Log.d(TAG, "saveUser: User saved successfully")
    }

    fun getUser(uid: String): Flow<UserEntity?> {
        Log.d(TAG, "getUser: Fetching user for UID: $uid")
        return userDao.getUserById(uid).onEach { user ->
            Log.d(TAG, "getUser: Received user update for $uid: ${user?.name}")
        }
    }

    // -------------------- Notes operations --------------------
    fun getActiveNotes(uid: String): Flow<List<NoteEntity>> {
        Log.d(TAG, "getActiveNotes: Fetching active notes for UID: $uid")
        return notesDao.getActiveNotes(uid).onEach { notes ->
            Log.d(TAG, "getActiveNotes: Found ${notes.size} active notes")
        }
    }

    fun getCompletedNotes(uid: String): Flow<List<NoteEntity>> {
        Log.d(TAG, "getCompletedNotes: Fetching completed notes for UID: $uid")
        return notesDao.getCompletedNotes(uid).onEach { notes ->
            Log.d(TAG, "getCompletedNotes: Found ${notes.size} completed notes")
        }
    }

    fun getNotesPaged(
        uid: String,
        searchQuery: String = "",
        sortField: String = "createdAt",
        sortOrder: String = "DESC",
        topic: String? = null,
        isCompleted: Boolean? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<PagingData<NoteEntity>> {
        val conditions = mutableListOf("userId = '$uid'")
        if (searchQuery.isNotEmpty()) {
            conditions.add("(title LIKE '%$searchQuery%' OR content LIKE '%$searchQuery%')")
        }
        if (topic != null && topic != "All") {
            conditions.add("topic = '$topic'")
        }
        if (isCompleted != null) {
            conditions.add("isCompleted = ${if (isCompleted) 1 else 0}")
        }
        if (startDate != null) {
            conditions.add("createdAt >= $startDate")
        }
        if (endDate != null) {
            conditions.add("createdAt <= $endDate")
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE ${conditions.joinToString(" AND ")}" else ""
        val sql = "SELECT * FROM notes $whereClause ORDER BY $sortField $sortOrder"
        
        Log.d(TAG, "getNotesPaged: SQL: $sql")
        val query = SimpleSQLiteQuery(sql)
        
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { notesDao.getNotesPaged(query) }
        ).flow
    }

    suspend fun addNote(note: NoteEntity) {
        Log.d(TAG, "addNote: Adding note: ${note.title}")
        notesDao.insertNote(note)
        Log.d(TAG, "addNote: Note added successfully")
    }

    suspend fun updateNote(note: NoteEntity) {
        Log.d(TAG, "updateNote: Updating note ${note.id}")
        notesDao.updateNote(
            noteId = note.id,
            title = note.title,
            content = note.content,
            topic = note.topic,
            reminderTime = note.reminderTime
        )
        Log.d(TAG, "updateNote: Note ${note.id} updated successfully")
    }


    suspend fun deleteNote(noteId: String) {
        Log.d(TAG, "deleteNote: Deleting note $noteId")
        notesDao.deleteNoteById(noteId)
        Log.d(TAG, "deleteNote: Note $noteId deleted successfully")
    }

    suspend fun updateNoteCompletion(noteId: String, isCompleted: Boolean) {
        Log.d(TAG, "updateNoteCompletion: Updating note $noteId completion to $isCompleted")
        notesDao.updateNoteCompletion(noteId, isCompleted)
    }

    fun getTodayReminders(uid: String): Flow<List<NoteEntity>> {
        Log.d(TAG, "getTodayReminders: Fetching today's reminders for UID: $uid")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return notesDao.getTodayReminders(uid, startOfDay, endOfDay).onEach { notes ->
            Log.d(TAG, "getTodayReminders: Found ${notes.size} reminders for today")
        }
    }

    // Reminder operations
    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    suspend fun deleteReminder(reminderId: Int) {
        reminderDao.deleteReminder(reminderId)
    }

    suspend fun getReminderById(reminderId: Int): Reminder? {
        return reminderDao.getReminderById(reminderId)
    }

    fun getActiveReminders(userId: String): Flow<List<Reminder>> {
        return reminderDao.getActiveReminders(userId)
    }

    fun getCompletedReminders(userId: String): Flow<List<Reminder>> {
        return reminderDao.getCompletedReminders(userId)
    }

    fun getRemindersByNote(noteId: Int): Flow<List<Reminder>> {
        return reminderDao.getRemindersByNote(noteId)
    }

    suspend fun getPendingReminders(currentTime: Long): List<Reminder> {
        return reminderDao.getPendingReminders(currentTime)
    }

    suspend fun getNoteById(noteId: String): NoteEntity? {
        Log.d(TAG, "getNoteById: Fetching note $noteId")
        return notesDao.getNoteById(noteId)
    }
}
