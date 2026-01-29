package com.example.takeanote1.data.repository

import android.util.Log
import com.example.takeanote1.data.local.dao.NotesDao
import com.example.takeanote1.data.local.dao.UserDao
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

class NotesRepository(
    private val notesDao: NotesDao,
    private val userDao: UserDao
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

    suspend fun getNoteById(noteId: String): NoteEntity? {
        Log.d(TAG, "getNoteById: Fetching note $noteId")
        return notesDao.getNoteById(noteId)
    }
}