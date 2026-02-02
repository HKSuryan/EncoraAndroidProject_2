package com.example.takeanote1.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
import java.util.Calendar
import java.util.UUID

/* ----------------------------- VIEW TYPE ----------------------------- */

enum class ViewType { LIST, GRID }

/* ----------------------------- UI STATES ----------------------------- */

data class DraftNoteState(
    val title: TextFieldValue = TextFieldValue(),
    val content: TextFieldValue = TextFieldValue(),
    val topic: String = "General",
    val reminderTime: Long? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val dateError: String? = null,
    val timeError: String? = null,
    val loaded: Boolean = false,
    val lastFocus: FocusTarget? = null
)

data class NotesFilter(
    val query: String = "",
    val sortField: String = "createdAt",
    val sortOrder: String = "DESC",
    val topic: String? = null,
    val dateRange: Pair<Long?, Long?> = null to null
)

/* ----------------------------- VIEWMODEL ----------------------------- */

class NotesViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences,
    private val notificationRepository: WorkManagerNotificationRepository
) : ViewModel() {

    /* ----------------------------- DRAFT ----------------------------- */

    var draft by mutableStateOf(DraftNoteState())
        private set

    // Convenience properties
    var draftTitle: TextFieldValue
        get() = draft.title
        set(value) { draft = draft.copy(title = value) }

    var draftContent: TextFieldValue
        get() = draft.content
        set(value) { draft = draft.copy(content = value) }


    fun updateDraft(block: DraftNoteState.() -> DraftNoteState) {
        draft = draft.block()
    }

    fun clearDraft() {
        draft = DraftNoteState(lastFocus = FocusTarget.TITLE)
    }

    fun loadNoteForEdit(noteId: String) {
        if (draft.loaded) return
        viewModelScope.launch {
            repository.getNoteById(noteId)?.let { note ->
                draft = draft.copy(
                    title = TextFieldValue(
                        text = note.title,
                        selection = TextRange(note.title.length) // cursor at end
                    ),
                    content = TextFieldValue(
                        text = note.content,
                        selection = TextRange(note.content.length) // cursor at end
                    ),
                    topic = note.topic,
                    reminderTime = note.reminderTime,
                    loaded = true,
                    dateError = null,
                    timeError = null
                )
            }
        }
    }
    fun getDayRange(millis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }


    /* ----------------------------- VIEW TYPE ----------------------------- */

    private val _viewType = MutableStateFlow(ViewType.LIST)
    val viewType: StateFlow<ViewType> = _viewType.asStateFlow()

    fun toggleViewType() {
        _viewType.value =
            if (_viewType.value == ViewType.LIST) ViewType.GRID else ViewType.LIST
    }

    /* ----------------------------- FILTERS (SOURCE OF TRUTH) ----------------------------- */

    private val _filters = MutableStateFlow(NotesFilter())
    val filters: StateFlow<NotesFilter> = _filters.asStateFlow()

//    val searchQuery: StateFlow<TextFieldValue> =
//        filters.map { TextFieldValue(it.query) }
//            .stateIn(viewModelScope, SharingStarted.Eagerly, TextFieldValue())
// In ViewModel
private val _searchQuery = MutableStateFlow(TextFieldValue())
    val searchQuery: StateFlow<TextFieldValue> = _searchQuery

    fun setSearchQuery(value: TextFieldValue) {

        _searchQuery.value = value
        _filters.update { it.copy(query = value.text) }
    }



    val topicFilter: StateFlow<String?> =
        filters.map { it.topic }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val dateRangeFilter: StateFlow<Pair<Long?, Long?>> =
        filters.map { it.dateRange }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null to null)

//    fun setSearchQuery(value: TextFieldValue) {
//        _filters.update { it.copy(query = value.text) }
//    }

    init {
        _searchQuery
            .onEach { _filters.update { it.copy(query = it.query) } }
            .launchIn(viewModelScope)
    }

    fun setSort(field: String, order: String) {
        _filters.update { it.copy(sortField = field, sortOrder = order) }
    }

    fun setTopicFilter(topic: String?) {
        _filters.update { it.copy(topic = topic) }
    }

    fun setDateRangeFilter(start: Long?, end: Long?) {
        val range = when {
            start != null && end != null -> getDayRange(start).first to getDayRange(end).second
            start != null -> getDayRange(start)
            end != null -> getDayRange(end)
            else -> null to null
        }
        _filters.update { it.copy(dateRange = range) }
    }

    fun clearFilters() {
        _filters.value = NotesFilter()
    }

    /* ----------------------------- PAGING ----------------------------- */

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filterWithUid =
        combine(userPreferences.userIdFlow, filters) { uid, filter ->
            uid to filter
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun notesPaging(isCompleted: Boolean): Flow<PagingData<NoteEntity>> =
        combine(userPreferences.userIdFlow, filters) { uid, filter -> uid to filter }
            .flatMapLatest { (uid, filter) ->
                if (uid == null) flowOf(PagingData.empty())
                else repository.getNotesPaged(
                    uid = uid,
                    searchQuery = filter.query.takeIf { it.isNotBlank() } ?: "",
                    sortField = filter.sortField,
                    sortOrder = filter.sortOrder,
                    topic = filter.topic,
                    isCompleted = isCompleted,
                    startDate = filter.dateRange.first, // can be null
                    endDate = filter.dateRange.second   // can be null
                )
            }
            .cachedIn(viewModelScope)



    val notesPaged = notesPaging(isCompleted = false)
    val completedNotesPaged = notesPaging(isCompleted = true)

    /* ----------------------------- NON-PAGING FLOWS ----------------------------- */

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeNotes: StateFlow<List<NoteEntity>> =
        userPreferences.userIdFlow
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else repository.getActiveNotes(uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val completedNotes: StateFlow<List<NoteEntity>> =
        userPreferences.userIdFlow
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else repository.getCompletedNotes(uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayReminders: StateFlow<List<NoteEntity>> =
        userPreferences.userIdFlow
            .flatMapLatest { uid ->
                if (uid == null) flowOf(emptyList())
                else repository.getTodayReminders(uid)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ----------------------------- CRUD ----------------------------- */

    fun addNote(title: String, content: String, topic: String, reminderTime: Long?) {
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
            reminderTime?.let {
                scheduleNotification(noteId, title, content, it)
            }
        }
    }

    fun updateNote(
        noteId: String,
        title: String,
        content: String,
        topic: String,
        reminderTime: Long?
    ) {
        viewModelScope.launch {
            val existing = repository.getNoteById(noteId) ?: return@launch

            repository.updateNote(
                existing.copy(
                    title = title,
                    content = content,
                    topic = topic,
                    reminderTime = reminderTime
                )
            )

            notificationRepository.cancelNotification(noteId)
            reminderTime?.let {
                scheduleNotification(noteId, title, content, it)
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            notificationRepository.cancelNotification(noteId)
        }
    }

    fun markAsCompleted(noteId: String) {
        viewModelScope.launch {
            repository.updateNoteCompletion(noteId, true)
            notificationRepository.cancelNotification(noteId)
        }
    }

    private fun scheduleNotification(
        noteId: String,
        title: String,
        content: String,
        reminderTime: Long
    ) {
        if (reminderTime <= System.currentTimeMillis()) return

        notificationRepository.scheduleNotificationAt(
            noteId = noteId,
            title = title,
            content = content,
            triggerTimeMillis = reminderTime
        )
    }


    suspend fun getNoteById(noteId: String): NoteEntity? =
        repository.getNoteById(noteId)

    /* ----------------------------- FACTORY ----------------------------- */

    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences,
        private val notificationRepository: WorkManagerNotificationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NotesViewModel(
                    repository,
                    userPreferences,
                    notificationRepository
                ) as T
            }
            error("Unknown ViewModel class")
        }
    }
}
