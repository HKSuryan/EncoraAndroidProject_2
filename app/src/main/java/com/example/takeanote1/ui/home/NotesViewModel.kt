package com.example.takeanote1.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.NoteEntity
import com.example.takeanote1.data.repository.NotesRepository
import com.example.takeanote1.data.repository.WorkManagerNotificationRepository
import com.example.takeanote1.ui.addnote.FocusTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class ViewType {
    LIST, GRID
}

class NotesViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences,
    private val notificationRepository: WorkManagerNotificationRepository
) : ViewModel() {

    // ---------------- ADD NOTE DRAFT STATE (Rotation Safe) ----------------
    var draftTitle by mutableStateOf("")
    var draftContent by mutableStateOf("")
    var draftTopic by mutableStateOf("General")
    var draftReminderTime by mutableStateOf<Long?>(null)

    var draftShowDatePicker by mutableStateOf(false)
    var draftShowTimePicker by mutableStateOf(false)

    var draftDateError by mutableStateOf<String?>(null)
    var draftTimeError by mutableStateOf<String?>(null)

    var draftLoaded by mutableStateOf(false) // Prevent reload on rotation
    // In NotesViewModel
    var lastFocusTarget: FocusTarget? by mutableStateOf(null)


    private val _viewType = MutableStateFlow(ViewType.LIST)
    val viewType: StateFlow<ViewType> = _viewType

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortField = MutableStateFlow("createdAt")
    val sortField: StateFlow<String> = _sortField

    private val _sortOrder = MutableStateFlow("DESC")
    val sortOrder: StateFlow<String> = _sortOrder

    private val _topicFilter = MutableStateFlow<String?>("All")
    val topicFilter: StateFlow<String?> = _topicFilter

    private val _dateRangeFilter = MutableStateFlow<Pair<Long?, Long?>>(null to null)

    val dateRangeFilter: StateFlow<Pair<Long?, Long?>> = _dateRangeFilter

    fun loadNoteForEdit(noteId: String) {
        if (draftLoaded) return
        viewModelScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            draftTitle = note.title
            draftContent = note.content
            draftTopic = note.topic
            draftReminderTime = note.reminderTime
            draftDateError = null
            draftTimeError = null
            draftLoaded = true
        }
    }

    fun clearDraft() {
        draftTitle = ""
        draftContent = ""
        draftTopic = "General"
        draftReminderTime = null
        draftShowDatePicker = false
        draftShowTimePicker = false
        draftDateError = null
        draftTimeError = null
        draftLoaded = false
        lastFocusTarget = FocusTarget.TITLE
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filtersFlow = combine(
        userPreferences.userIdFlow,
        _searchQuery,
        _sortField,
        _sortOrder,
        _topicFilter,
        _dateRangeFilter
    ) { array ->
        Filters(
            uid = array[0] as String?,
            query = array[1] as String,
            field = array[2] as String,
            order = array[3] as String,
            topic = array[4] as String?,
            dateRange = array[5] as Pair<Long?, Long?>
        )
    }

    // ---------------- Paging Flows ----------------
    @OptIn(ExperimentalCoroutinesApi::class)
    val notesPaged: Flow<PagingData<NoteEntity>> = filtersFlow.flatMapLatest { filters ->
        val uid = filters.uid
        if (uid == null) flowOf(PagingData.empty())
        else repository.getNotesPaged(
            uid = uid,
            searchQuery = filters.query,
            sortField = filters.field,
            sortOrder = filters.order,
            topic = filters.topic,
            isCompleted = false,
            startDate = filters.dateRange.first,
            endDate = filters.dateRange.second
        )
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val completedNotesPaged: Flow<PagingData<NoteEntity>> = filtersFlow.flatMapLatest { filters ->
        val uid = filters.uid
        if (uid == null) flowOf(PagingData.empty())
        else repository.getNotesPaged(
            uid = uid,
            searchQuery = filters.query,
            sortField = filters.field,
            sortOrder = filters.order,
            topic = filters.topic,
            isCompleted = true,
            startDate = filters.dateRange.first,
            endDate = filters.dateRange.second
        )
    }.cachedIn(viewModelScope)

    // ---------------- List Flows ----------------
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeNotes: StateFlow<List<NoteEntity>> = userPreferences.userIdFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.getActiveNotes(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val completedNotes: StateFlow<List<NoteEntity>> = userPreferences.userIdFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.getCompletedNotes(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayReminders: StateFlow<List<NoteEntity>> = userPreferences.userIdFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else repository.getTodayReminders(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------------- UI & Filters ----------------
    fun toggleViewType() { _viewType.value = if (_viewType.value == ViewType.LIST) ViewType.GRID else ViewType.LIST }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSort(field: String, order: String) { _sortField.value = field; _sortOrder.value = order }
    fun setTopicFilter(topic: String?) { _topicFilter.value = topic }
    fun setDateRangeFilter(start: Long?, end: Long?) { _dateRangeFilter.value = start to end }
    fun clearFilters() { _searchQuery.value = ""; _topicFilter.value = "All"; _dateRangeFilter.value = null to null }

    // ---------------- Note Operations ----------------
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
            reminderTime?.let { scheduleNotification(noteId, title, content, it) }
        }
    }

    fun updateNote(noteId: String, title: String, content: String, topic: String, reminderTime: Long?) {
        viewModelScope.launch {
            val existingNote = repository.getNoteById(noteId) ?: return@launch
            val updatedNote = existingNote.copy(title = title, content = content, topic = topic, reminderTime = reminderTime)
            repository.updateNote(updatedNote)
            notificationRepository.cancelNotification(noteId)
            reminderTime?.let { scheduleNotification(noteId, title, content, it) }
        }
    }

    fun deleteNote(noteId: String) { viewModelScope.launch { repository.deleteNote(noteId); notificationRepository.cancelNotification(noteId) } }

    fun markAsCompleted(noteId: String) { viewModelScope.launch { repository.updateNoteCompletion(noteId, true); notificationRepository.cancelNotification(noteId) } }

    private fun scheduleNotification(noteId: String, title: String, content: String, reminderTime: Long) {
        if (reminderTime <= System.currentTimeMillis()) return
        notificationRepository.scheduleNotificationAt(noteId, title, content, reminderTime)
    }

    suspend fun getNoteById(noteId: String): NoteEntity? = repository.getNoteById(noteId)

    // ---------------- Factory ----------------
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

    data class Filters(val uid: String?, val query: String, val field: String, val order: String, val topic: String?, val dateRange: Pair<Long?, Long?>)
}
