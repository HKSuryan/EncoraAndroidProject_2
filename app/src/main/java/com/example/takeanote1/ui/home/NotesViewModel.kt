package com.example.takeanote1.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class NotesViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val TAG = "NotesViewModel"

    private val _activeNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val activeNotes: StateFlow<List<NoteEntity>> = _activeNotes

    private val _completedNotes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val completedNotes: StateFlow<List<NoteEntity>> = _completedNotes
    private val _selectedCompletedNotes = MutableStateFlow<Set<String>>(emptySet())
    val selectedCompletedNotes: StateFlow<Set<String>> = _selectedCompletedNotes
    private val _todayReminders = MutableStateFlow<List<NoteEntity>>(emptyList())
    val todayReminders: StateFlow<List<NoteEntity>> = _todayReminders

    init {
        Log.d(TAG, "init: Initializing NotesViewModel")
        
        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                Log.d(TAG, "collectLatest: Received UID: $uid for Active Notes")
                uid?.let {
                    repository.getActiveNotes(it).collect { notes ->
                        Log.d(TAG, "getActiveNotes: Collected ${notes.size} notes for $it")
                        _activeNotes.value = notes
                    }
                }
            }
        }
        
        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                Log.d(TAG, "collectLatest: Received UID: $uid for Completed Notes")
                uid?.let {
                    repository.getCompletedNotes(it).collect { notes ->
                        Log.d(TAG, "getCompletedNotes: Collected ${notes.size} notes for $it")
                        _completedNotes.value = notes
                    }
                }
            }
        }

        viewModelScope.launch {
            userPreferences.userIdFlow.collectLatest { uid ->
                Log.d(TAG, "collectLatest: Received UID: $uid for Today Reminders")
                uid?.let {
                    repository.getTodayReminders(it).collect { notes ->
                        Log.d(TAG, "getTodayReminders: Collected ${notes.size} notes for $it")
                        _todayReminders.value = notes
                    }
                }
            }
        }
    }

    fun addNote(title: String, content: String, topic: String, reminderTime: Long? = null) {
        Log.d(TAG, "addNote: Adding note with title: $title")
        viewModelScope.launch {
            val uid = userPreferences.userIdFlow.first()
            Log.d(TAG, "addNote: Using UID: $uid")
            uid?.let {
                val note = NoteEntity(
                    id = UUID.randomUUID().toString(),
                    userId = it,
                    title = title,
                    content = content,
                    topic = topic,
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    reminderTime = reminderTime
                )
                repository.addNote(note)
                Log.d(TAG, "addNote: Note added successfully")
            } ?: Log.w(TAG, "addNote: UID is null, cannot add note")
        }
    }

    fun markAsCompleted(noteId: String) {
        Log.d(TAG, "markAsCompleted: Marking note $noteId as completed")
        viewModelScope.launch {
            repository.updateNoteCompletion(noteId, true)
            Log.d(TAG, "markAsCompleted: Note $noteId updated")
        }
    }
    fun deleteAllCompletedNotes() {
        Log.d(TAG, "deleteAllCompletedNotes: Deleting all completed notes")
        viewModelScope.launch {
            val uid = userPreferences.userIdFlow.first()
            uid?.let {
                repository.deleteAllCompletedNotes(it)
                Log.d(TAG, "deleteAllCompletedNotes: Completed")
            } ?: Log.w(TAG, "deleteAllCompletedNotes: UID is null")
        }
    }
    fun toggleCompletedNoteSelection(noteId: String) {
        _selectedCompletedNotes.value =
            if (_selectedCompletedNotes.value.contains(noteId))
                _selectedCompletedNotes.value - noteId
            else
                _selectedCompletedNotes.value + noteId
    }
    fun deleteSelectedCompletedNotes() {
        Log.d(TAG, "deleteSelectedCompletedNotes: Deleting selected completed notes")
        viewModelScope.launch {
            val uid = userPreferences.userIdFlow.first()
            uid?.let {
                repository.deleteSelectedCompletedNotes(it, _selectedCompletedNotes.value.toList())
                _selectedCompletedNotes.value = emptySet()
                Log.d(TAG, "deleteSelectedCompletedNotes: Completed")
            }
        }
    }


    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("NotesViewModelFactory", "create: Creating NotesViewModel")
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}