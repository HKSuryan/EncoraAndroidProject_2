package com.example.takeanote1.ui.components

import android.R.attr.maxHeight
import android.content.res.Configuration
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.ui.platform.LocalConfiguration

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



@OptIn(ExperimentalMaterial3Api::class)
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(max = if (isLandscape) 600.dp else 400.dp) // max width
            .heightIn(max = 600.dp), // max height for both orientations
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp)
            ) {
                // Topics
                Text(
                    "By Topic:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Date Range
                Text(
                    "By Date Range:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

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

                // DatePicker with fixed height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // fixed height, enough for portrait & landscape
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
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateRangeSelected(state.selectedStartDateMillis, state.selectedEndDateMillis)
                    onDismiss()
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
