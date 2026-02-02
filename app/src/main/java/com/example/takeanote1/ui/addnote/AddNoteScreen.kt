package com.example.takeanote1.ui.addnote

import android.R.attr.content
import android.R.attr.title
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.home.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*


/* ------------------ Focus Tracking ------------------ */
enum class FocusTarget { TITLE, CONTENT }

/* ------------------ Disable Past Dates ------------------ */
@OptIn(ExperimentalMaterial3Api::class)
object FutureDatesOnly : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return utcTimeMillis >= todayStart
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    noteId: String? = null,
    onBack: () -> Unit
) {
    val isEditing = noteId != null

    /* ------------------ VM-backed draft state ------------------ */
    val draft by remember { viewModel::draft }

    /* ------------------ Focus ------------------ */
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }

    // Restore focus safely after rotation using ViewModel
    LaunchedEffect(draft.lastFocus) {
        when (draft.lastFocus) {
            FocusTarget.TITLE -> titleFocusRequester.requestFocus()
            FocusTarget.CONTENT -> contentFocusRequester.requestFocus()
            null -> if (!isEditing) titleFocusRequester.requestFocus()
        }
    }

    /* ------------------ Scroll ------------------ */
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val topics = listOf("General", "Work", "Personal", "Shopping", "Health", "Ideas")

    /* ------------------ Date Picker ------------------ */
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = draft.reminderTime ?: System.currentTimeMillis(),
        selectableDates = FutureDatesOnly
    )

    /* ------------------ Time Picker ------------------ */
    val cal = Calendar.getInstance().apply {
        timeInMillis = draft.reminderTime ?: System.currentTimeMillis()
    }
    val timePickerState = rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = false
    )

    /* ------------------ Load edit note ------------------ */
    LaunchedEffect(noteId) {
        if (isEditing && !draft.loaded) {
            viewModel.loadNoteForEdit(noteId!!)
        }
    }

    /* ------------------ DATE PICKER ------------------ */
    if (draft.showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { viewModel.updateDraft { copy(showDatePicker = false) } },
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
                        viewModel.updateDraft { copy(dateError = "Please select today or a future date") }
                        return@TextButton
                    }

                    viewModel.updateDraft {
                        copy(
                            dateError = null,
                            showDatePicker = false,
                            showTimePicker = true
                        )
                    }
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateDraft { copy(showDatePicker = false) } }) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                DatePicker(state = datePickerState)
                draft.dateError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    /* ------------------ TIME PICKER ------------------ */
    if (draft.showTimePicker) {
        AlertDialog(
            onDismissRequest = { viewModel.updateDraft { copy(showTimePicker = false) } },
            title = { Text("Select Time") },
            text = {
                Column {
                    TimePicker(state = timePickerState)
                    draft.timeError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis ?: return@TextButton
                    val finalCal = Calendar.getInstance().apply {
                        timeInMillis = date
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    if (finalCal.timeInMillis <= System.currentTimeMillis()) {
                        viewModel.updateDraft { copy(timeError = "Please select a future time") }
                        return@TextButton
                    }

                    viewModel.updateDraft {
                        copy(
                            reminderTime = finalCal.timeInMillis,
                            timeError = null,
                            showTimePicker = false
                        )
                    }
                }) { Text("Set Reminder") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.updateDraft { copy(showTimePicker = false) } }) {
                    Text("Cancel")
                }
            }
        )
    }

    /* ------------------ MAIN UI ------------------ */
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
                .verticalScroll(verticalScroll)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ------------------ Title ------------------ */
            OutlinedTextField(
                value = draft.title,
                onValueChange = { viewModel.updateDraft { copy(title = it) } },
                label = { Text("Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) viewModel.updateDraft { copy(lastFocus = FocusTarget.TITLE) }
                    },
                singleLine = true
            )




            /* ------------------ Topic ------------------ */
            Text("Select Topic", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach {
                    FilterChip(
                        selected = draft.topic == it,
                        onClick = { viewModel.updateDraft { copy(topic = it) } },
                        label = { Text(it) }
                    )
                }
            }

            /* ------------------ Content ------------------ */
            OutlinedTextField(
                value = draft.content,
                onValueChange = { viewModel.updateDraft { copy(content = it) } },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp)
                    .focusRequester(contentFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) viewModel.updateDraft { copy(lastFocus = FocusTarget.CONTENT) }
                    },
                minLines = 5
            )


            /* ------------------ Reminder Display ------------------ */
            draft.reminderTime?.let {
                val formatted = SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault()).format(it)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminder: $formatted", color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { viewModel.updateDraft { copy(reminderTime = null) } }) {
                        Text("Clear")
                    }
                }
            }

            /* ------------------ Set/Update Reminder ------------------ */
            Button(
                onClick = { viewModel.updateDraft { copy(showDatePicker = true) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (draft.reminderTime == null) "Set Reminder" else "Update Reminder")
            }

            /* ------------------ Save / Update Note ------------------ */
            Button(
                onClick = {
                    if (isEditing) {
                        viewModel.updateNote(
                            noteId!!,
                            draft.title.text,
                            draft.content.text,
                            draft.topic,
                            draft.reminderTime
                        )
                    } else {
                        viewModel.addNote(
                            draft.title.text,
                            draft.content.text,
                            draft.topic,
                            draft.reminderTime
                        )
                    }
                    viewModel.clearDraft()
                    onBack()
                },
                enabled = draft.title.text.isNotBlank() && draft.content.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Note" else "Save Note")
            }

        }
    }
}
