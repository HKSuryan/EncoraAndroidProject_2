package com.example.takeanote1.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.takeanote1.data.repository.NotesRepository
import com.example.takeanote1.data.datastore.UserPreferences
import com.example.takeanote1.data.local.entity.Reminder
import com.example.takeanote1.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedTime: Pair<Int, Int> = Pair(12, 0) // hour, minute
)

class ReminderViewModel(
    private val repository: NotesRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderUiState())
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val userId = userPreferences.userIdFlow.first() ?: ""
                repository.getActiveReminders(userId).collect { reminders ->
                    _uiState.value = _uiState.value.copy(
                        reminders = reminders,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setSelectedDate(dateInMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = dateInMillis)
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(selectedTime = Pair(hour, minute))
    }

    fun createReminder(
        noteId: Int?,
        title: String,
        description: String,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.userIdFlow.first() ?: ""

                val calendar = Calendar.getInstance().apply {
                    timeInMillis = _uiState.value.selectedDate
                    set(Calendar.HOUR_OF_DAY, _uiState.value.selectedTime.first)
                    set(Calendar.MINUTE, _uiState.value.selectedTime.second)
                    set(Calendar.SECOND, 0)
                }

                val reminder = Reminder(
                    noteId = noteId,
                    title = title,
                    description = description,
                    reminderDateTime = calendar.timeInMillis,
                    userId = userId
                )

                val reminderId = repository.insertReminder(reminder).toInt()

                // Schedule the alarm
                val scheduler = ReminderScheduler(context)
                scheduler.scheduleReminder(
                    reminderId = reminderId,
                    reminderTime = calendar.timeInMillis,
                    title = title,
                    description = description
                )

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                repository.updateReminder(reminder)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteReminder(reminderId: Int, context: Context) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminderId)

                // Cancel the scheduled alarm
                val scheduler = ReminderScheduler(context)
                scheduler.cancelReminder(reminderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun markReminderComplete(reminderId: Int) {
        viewModelScope.launch {
            try {
                val reminder = repository.getReminderById(reminderId)
                reminder?.let {
                    repository.updateReminder(it.copy(isCompleted = true))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    class Factory(
        private val repository: NotesRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
                return ReminderViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}