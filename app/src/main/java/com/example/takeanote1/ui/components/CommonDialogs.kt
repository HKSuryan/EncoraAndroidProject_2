package com.example.takeanote1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SortDialog(
    onDismiss: () -> Unit,
    onSortSelected: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort By") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                SortItem("Date (Newest First)") {
                    onSortSelected("createdAt", "DESC")
                }
                SortItem("Date (Oldest First)") {
                    onSortSelected("createdAt", "ASC")
                }
                SortItem("Title (A–Z)") {
                    onSortSelected("title", "ASC")
                }
                SortItem("Title (Z–A)") {
                    onSortSelected("title", "DESC")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SortItem(
    title: String,
    onSelect: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            TextButton(onClick = onSelect) {
                Text("Select")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onTopicSelected: (String?) -> Unit,
    onDateRangeSelected: (Long?, Long?) -> Unit,
    currentTopic: String?,
    currentDateRange: Pair<Long?, Long?>,
    onClearFilters: () -> Unit
) {
    val topics = listOf("All", "Work", "Personal", "Study", "Ideas", "Other")
    val state = rememberDateRangePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filter Options")
                TextButton(onClick = onClearFilters) { Text("Clear All") }
            }
        },
        text = {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                val maxHeightAvailable = maxHeight

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Topic Chips
                    Text("By Topic:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        topics.forEach { topic ->
                            FilterChip(
                                selected = currentTopic == topic,
                                onClick = { onTopicSelected(topic) },
                                label = { Text(topic) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Range
                    Text("By Date Range:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (currentDateRange.first != null && currentDateRange.second != null) {
                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val start = sdf.format(Date(currentDateRange.first!!))
                        val end = sdf.format(Date(currentDateRange.second!!))
                        Text(
                            "Current: $start - $end",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic height for DatePicker
                    val remainingHeight = maxHeightAvailable - 200.dp // leave space for chips, texts, buttons
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = remainingHeight)
                    ) {
                        DateRangePicker(
                            state = state,
                            modifier = Modifier.fillMaxSize(),
                            title = null,
                            headline = null,
                            showModeToggle = false
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateRangeSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}
