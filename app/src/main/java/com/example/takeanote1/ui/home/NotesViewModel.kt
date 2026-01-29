package com.example.takeanote1.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.repository.NotesRepository
import com.example.takeanote1.data.repository.WorkManagerNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class NotesViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences,
    private val notificationRepository: WorkManagerNotificationRepository
) : ViewModel() {

    private val TAG = "NotesViewModel"

    private val _activeNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val activeNotes: StateFlow<List<NoteEntity>> = _activeNotes

    private val _completedNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val completedNotes: StateFlow<List<NoteEntity>> = _completedNotes

    private val _todayReminders = MutableStateFlow<List<NoteEntity>>(emptyList())
    val todayReminders: StateFlow<List<NoteEntity>> = _todayReminders

    init {
        Log.d(TAG, "init: Initializing NotesViewModel")

        // Observe active notes
        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                uid?.let {
                    repository.getActiveNotes(it).collect { notes ->
                        _activeNotes.value = notes
                    }
                }
            }
        }

        // Observe completed notes
        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                uid?.let {
                    repository.getCompletedNotes(it).collect { notes ->
                        _completedNotes.value = notes
                    }
                }
            }
        }

        // Observe today reminders
        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                uid?.let {
                    repository.getTodayReminders(it).collect { notes ->
                        _todayReminders.value = notes
                    }
                }
            }
        }
    }

    /** Add a new note */
    fun addNote(title: String, content: String, topic: String, reminderTime: Long? = null) {
        viewModelScope.launch {
            val uid = userPreferences.userIdFlow.first() ?: return@launch
            val noteId = UUID.randomUUID().toString()
            val note = NoteEntity(
                id = noteId,
                userId = uid,
                title = title,
                content = content,
                topic = topic,
                isCompleted = false,
                createdAt = System.currentTimeMillis(),
                reminderTime = reminderTime
            )
            repository.addNote(note)

            // Schedule notification if reminderTime is valid
            reminderTime?.let { time ->
                scheduleNotification(noteId, title, content, time)
            }
        }
    }

    /** Update existing note */
    fun updateNote(
        noteId: String,
        title: String,
        content: String,
        topic: String,
        reminderTime: Long?
    ) {
        viewModelScope.launch {
            val existingNote = repository.getNoteById(noteId) ?: return@launch
            val updatedNote = existingNote.copy(
                title = title,
                content = content,
                topic = topic,
                reminderTime = reminderTime
            )
            repository.updateNote(updatedNote)

            // Cancel old notification
            notificationRepository.cancelNotification(noteId)

            // Schedule new notification if valid
            reminderTime?.let { time ->
                scheduleNotification(noteId, title, content, time)
            }
        }
    }

    /** Delete a note */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            // Cancel any scheduled notifications
            notificationRepository.cancelNotification(noteId)
        }
    }

    /** Mark note as completed */
    fun markAsCompleted(noteId: String) {
        viewModelScope.launch {
            repository.updateNoteCompletion(noteId, true)
            // Cancel any scheduled notifications
            notificationRepository.cancelNotification(noteId)
        }
    }

    /** Schedule a WorkManager notification */
    private fun scheduleNotification(noteId: String, title: String, content: String, reminderTime: Long) {
        val now = System.currentTimeMillis()
        if (reminderTime <= now) {
            Log.w(TAG, "scheduleNotification: Reminder time is in the past. Skipping note $noteId")
            return
        }
        notificationRepository.scheduleNotificationAt(noteId, title, content, reminderTime)
    }

    /** Fetch note by ID */
    suspend fun getNoteById(noteId: String): NoteEntity? {
        return repository.getNoteById(noteId)
    }

    /** ViewModel Factory */
    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences,
        private val notificationRepository: WorkManagerNotificationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(repository, userPreferences, notificationRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}