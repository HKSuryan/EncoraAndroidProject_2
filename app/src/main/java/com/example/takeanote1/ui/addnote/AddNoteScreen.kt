package com.example.takeanote1.ui.addnote

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.home.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*

/** Tracks which field had focus (rotation-safe) */
enum class FocusTarget {
    TITLE, CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onBack: () -> Unit
) {
    val isEditing = noteId != null

    // ---- ViewModel-backed state (rotation safe) ----
    val title by remember { derivedStateOf { viewModel.draftTitle } }
    val content by remember { derivedStateOf { viewModel.draftContent } }
    val topic by remember { derivedStateOf { viewModel.draftTopic } }
    val reminderTime by remember { derivedStateOf { viewModel.draftReminderTime } }
    val showDatePicker by remember { derivedStateOf { viewModel.draftShowDatePicker } }
    val showTimePicker by remember { derivedStateOf { viewModel.draftShowTimePicker } }
    val dateError by remember { derivedStateOf { viewModel.draftDateError } }
    val timeError by remember { derivedStateOf { viewModel.draftTimeError } }

    // ---- Focus handling (rotation safe) ----
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }

    var lastFocus by rememberSaveable { mutableStateOf<FocusTarget?>(null) }
    var focusRestored by rememberSaveable { mutableStateOf(false) }

    // ---- Scroll ----
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val topics = listOf("General", "Work", "Personal", "Shopping", "Health", "Ideas")

    // ---- Date Picker ----
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = reminderTime ?: System.currentTimeMillis()
    )

    // ---- Time Picker ----
    val calendar = Calendar.getInstance().apply {
        timeInMillis = reminderTime ?: System.currentTimeMillis()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )

    // ---- Load note when editing ----
    LaunchedEffect(noteId) {
        if (isEditing && !viewModel.draftLoaded) {
            viewModel.loadNoteForEdit(noteId!!)
        }
    }

    // ---- Restore EXACT focus after rotation ----
    LaunchedEffect(lastFocus, isEditing) {
        if (!focusRestored) {
            when (lastFocus) {
                FocusTarget.TITLE -> titleFocusRequester.requestFocus()
                FocusTarget.CONTENT -> contentFocusRequester.requestFocus()
                null -> {
                    if (isEditing) {
                        contentFocusRequester.requestFocus()
                    } else {
                        titleFocusRequester.requestFocus()
                    }
                }
            }
            focusRestored = true
        }
    }

    // ---------- DATE PICKER ----------
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.draftShowDatePicker = false },
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
                        viewModel.draftDateError = "Please select a future date"
                        return@TextButton
                    }

                    viewModel.draftDateError = null
                    viewModel.draftShowDatePicker = false
                    viewModel.draftShowTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.draftShowDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)
                dateError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // ---------- TIME PICKER ----------
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.draftShowTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Column {
                    TimePicker(state = timePickerState)
                    timeError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis ?: return@TextButton

                    val cal = Calendar.getInstance().apply {
                        timeInMillis = date
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (cal.timeInMillis <= System.currentTimeMillis()) {
                        viewModel.draftTimeError = "Please select a future time"
                        return@TextButton
                    }

                    viewModel.draftReminderTime = cal.timeInMillis
                    viewModel.draftTimeError = null
                    viewModel.draftShowTimePicker = false
                }) { Text("Set Reminder") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.draftShowTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------- MAIN UI ----------
    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isEditing) "Edit Note" else "Add Note",
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

            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.draftTitle = it },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) lastFocus = FocusTarget.TITLE
                    }
            )

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
                        onClick = { viewModel.draftTopic = it },
                        label = { Text(it) }
                    )
                }
            }

            OutlinedTextField(
                value = content,
                onValueChange = { viewModel.draftContent = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .focusRequester(contentFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) lastFocus = FocusTarget.CONTENT
                    },
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
                    Text("Reminder: $formatted", color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { viewModel.draftReminderTime = null }) {
                        Text("Clear")
                    }
                }
            }

            Button(
                onClick = { viewModel.draftShowDatePicker = true },
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
