package com.example.takeanote1.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    onAddNoteClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRemindersClick: () -> Unit
) {
    val activeNotes by viewModel.activeNotes.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Notes",
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History"
                        )
                    }
                    IconButton(onClick = onRemindersClick) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reminders"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            if (todayReminders.isNotEmpty()) {
                item {
                    Text(
                        text = "Today's Reminders",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                items(todayReminders) { note ->
                    NoteCard(
                        note = note,
                        onCompleteClick = { viewModel.markAsCompleted(note.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            item {
                Text(
                    text = "Active Notes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            if (activeNotes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No active notes. Create one!")
                    }
                }
            } else {
                items(activeNotes) { note ->
                    NoteCard(
                        note = note,
                        onCompleteClick = { viewModel.markAsCompleted(note.id) }
                    )
                }
            }
        }
    }
}
