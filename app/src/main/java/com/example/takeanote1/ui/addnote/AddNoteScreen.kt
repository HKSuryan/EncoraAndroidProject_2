package com.example.takeanote1.ui.addnote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.home.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onBack: () -> Unit
) {
    val isEditing = noteId != null

    // --- Use ViewModel-backed draft state ---
    var title by remember { mutableStateOf(viewModel.draftTitle) }
    var content by remember { mutableStateOf(viewModel.draftContent) }
    var topic by remember { mutableStateOf(viewModel.draftTopic) }
    var reminderTime by remember { mutableStateOf(viewModel.draftReminderTime) }
    var showDatePicker by remember { mutableStateOf(viewModel.draftShowDatePicker) }
    var showTimePicker by remember { mutableStateOf(viewModel.draftShowTimePicker) }
    var dateError by remember { mutableStateOf(viewModel.draftDateError) }
    var timeError by remember { mutableStateOf(viewModel.draftTimeError) }

    val topics = listOf("General", "Work", "Personal", "Shopping", "Health", "Ideas")
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = reminderTime ?: System.currentTimeMillis()
    )

    val timePickerState = rememberTimePickerState(
        initialHour = if (reminderTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminderTime!! }.get(Calendar.HOUR_OF_DAY)
        } else 0,
        initialMinute = if (reminderTime != null) {
            Calendar.getInstance().apply { timeInMillis = reminderTime!! }.get(Calendar.MINUTE)
        } else 0,
        is24Hour = false
    )

    // --- Load existing note if editing ---
    LaunchedEffect(noteId) {
        if (isEditing) {
            viewModel.getNoteById(noteId!!)?.let { note ->
                title = note.title
                content = note.content
                topic = note.topic
                reminderTime = note.reminderTime
            }
        }
    }

    // --- DATE PICKER ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val todayStart = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    if (selectedDate < todayStart) {
                        dateError = "Please select a future date"
                        viewModel.draftDateError = dateError
                        return@TextButton
                    }

                    dateError = null
                    viewModel.draftDateError = null
                    showDatePicker = false
                    viewModel.draftShowDatePicker = false
                    showTimePicker = true
                    viewModel.draftShowTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    viewModel.draftShowDatePicker = false
                }) { Text("Cancel") }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)
                dateError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // --- TIME PICKER ---
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
                viewModel.draftShowTimePicker = false
            },
            title = { Text("Select Time") },
            text = {
                Column {
                    TimePicker(state = timePickerState)
                    timeError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    if (cal.timeInMillis <= System.currentTimeMillis()) {
                        timeError = "Please select a future time"
                        viewModel.draftTimeError = timeError
                        return@TextButton
                    }
                    reminderTime = cal.timeInMillis
                    viewModel.draftReminderTime = reminderTime
                    timeError = null
                    viewModel.draftTimeError = null
                    showTimePicker = false
                    viewModel.draftShowTimePicker = false
                }) { Text("Set Reminder") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    viewModel.draftShowTimePicker = false
                }) { Text("Cancel") }
            }
        )
    }

    // --- MAIN UI ---
    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isEditing) "Edit Note" else "Add New Note",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    viewModel.draftTitle = it
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Topic Chips
            Text("Select Topic", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach {
                    FilterChip(
                        selected = topic == it,
                        onClick = {
                            topic = it
                            viewModel.draftTopic = it
                        },
                        label = { Text(it) }
                    )
                }
            }

            // Content
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    viewModel.draftContent = it
                },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                minLines = 5
            )

            // Reminder display
            reminderTime?.let {
                val formatted = SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault())
                    .format(it)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Reminder set: $formatted", color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = {
                        reminderTime = null
                        viewModel.draftReminderTime = null
                    }) { Text("Clear") }
                }
            }

            // Set Reminder Button
            Button(
                onClick = {
                    showDatePicker = true
                    viewModel.draftShowDatePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (reminderTime == null) "Set Reminder" else "Update Reminder")
            }

            // Save / Update Button
            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateNote(noteId!!, title, content, topic, reminderTime)
                    } else {
                        viewModel.addNote(title, content, topic, reminderTime)
                    }

                    // Clear draft after saving
                    viewModel.clearDraft()

                    onBack()
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Note" else "Save Note")
            }
        }
    }
}
