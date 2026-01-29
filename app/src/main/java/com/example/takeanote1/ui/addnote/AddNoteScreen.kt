package com.example.takeanote1.ui.addnote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("General") }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    val isEditing = noteId != null

    // Load existing note if editing
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }

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

    val topics = listOf("General", "Work", "Personal", "Shopping", "Health", "Ideas")
    val scrollState = rememberScrollState()

    // ---------- DATE PICKER ----------
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate =
                            datePickerState.selectedDateMillis ?: return@TextButton

                        val todayStart = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        if (selectedDate < todayStart) {
                            dateError = "Please select a future date"
                            return@TextButton
                        }

                        dateError = null
                        showDatePicker = false
                        showTimePicker = true
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)

                dateError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // ---------- TIME PICKER ----------
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column {
                    TimePicker(state = timePickerState)

                    timeError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDate =
                            datePickerState.selectedDateMillis ?: return@TextButton

                        val selectedCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (selectedCal.timeInMillis <= System.currentTimeMillis()) {
                            timeError = "Please select a future time"
                            return@TextButton
                        }

                        reminderTime = selectedCal.timeInMillis
                        timeError = null
                        showTimePicker = false
                    }
                ) {
                    Text("Set Reminder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------- UI ----------
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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Select Topic", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach {
                    FilterChip(
                        selected = topic == it,
                        onClick = { topic = it },
                        label = { Text(it) }
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 5
            )

            reminderTime?.let {
                val formatted = SimpleDateFormat(
                    "EEE, MMM d, hh:mm a",
                    Locale.getDefault()
                ).format(it)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Reminder set: $formatted",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { reminderTime = null }) {
                        Text("Clear")
                    }
                }
            }

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (reminderTime == null) "Set Reminder" else "Update Reminder")
            }

            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateNote(noteId!!, title, content, topic, reminderTime)
                    } else {
                        viewModel.addNote(title, content, topic, reminderTime)
                    }
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
