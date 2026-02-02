package com.example.takeanote1.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    noteId: Int? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Focus requester for title field
    val titleFocusRequester = remember { FocusRequester() }

    // Local state synced with ViewModel
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var selectedDate by rememberSaveable { mutableStateOf(uiState.selectedDate) }
    var selectedHour by rememberSaveable { mutableStateOf(uiState.selectedTime.first) }
    var selectedMinute by rememberSaveable { mutableStateOf(uiState.selectedTime.second) }

    // Formatters
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Combined Calendar for display
    val selectedDateTime = remember(selectedDate, selectedHour, selectedMinute) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
    }

    // Sync local state with ViewModel updates (survive rotation)
    LaunchedEffect(uiState.selectedDate, uiState.selectedTime) {
        selectedDate = uiState.selectedDate
        selectedHour = uiState.selectedTime.first
        selectedMinute = uiState.selectedTime.second
    }

    // Request focus on title initially
    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }

    Scaffold(
        snackbarHost = {
            if (showError) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { showError = false }) { Text("Dismiss") } }
                ) { Text(errorMessage) }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val nowMillis = System.currentTimeMillis()
                    if (title.isNotBlank()) {
                        if (selectedDateTime.timeInMillis > nowMillis) {
                            viewModel.createReminder(noteId, title, description, context) {
                                // Reset fields
                                title = ""
                                description = ""
                                val now = Calendar.getInstance()
                                selectedDate = now.timeInMillis
                                selectedHour = now.get(Calendar.HOUR_OF_DAY)
                                selectedMinute = now.get(Calendar.MINUTE)

                                // Update ViewModel
                                viewModel.setSelectedDate(selectedDate)
                                viewModel.setSelectedTime(selectedHour, selectedMinute)

                                onBack()
                            }
                        } else {
                            errorMessage = "Please select a future date and time"
                            showError = true
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Reminder")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "New Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Enter reminder title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add more details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Date & Time Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Date Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
                            ) { showDatePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateFormat.format(selectedDateTime.time), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Time Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(color = MaterialTheme.colorScheme.primary)
                            ) { showTimePicker = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(timeFormat.format(selectedDateTime.time), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // ------------------- Date Picker -------------------
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        if (selected >= System.currentTimeMillis()) {
                            selectedDate = selected
                            viewModel.setSelectedDate(selected) // ✅ Update ViewModel
                        } else {
                            showError = true
                            errorMessage = "Please select a future date"
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ------------------- Time Picker -------------------
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedCal = Calendar.getInstance().apply {
                        timeInMillis = selectedDate
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                    }
                    if (selectedCal.timeInMillis > System.currentTimeMillis()) {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false

                        // ✅ Update ViewModel
                        viewModel.setSelectedTime(selectedHour, selectedMinute)
                    } else {
                        showError = true
                        errorMessage = "Please select a future time"
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TimePickerDefaults.colors(
                            clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                            clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectorColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                            periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        )
    }

    // Handle ViewModel errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            errorMessage = error
            showError = true
        }
    }
}
