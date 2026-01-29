package com.example.takeanote1.ui.home

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.auth.AuthUiState
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.components.AppTopBar
import com.example.takeanote1.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NotesViewModel,
    authViewModel: AuthViewModel,              // Added ViewModel for auth actions
    onAddNoteClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRemindersClick: () -> Unit
    onEditNoteClick: (String) -> Unit, // Pass noteId to edit screen
    onLoginNavigate: () -> Unit                // Added navigation callback
) {

    val context = LocalContext.current
    val activity = context as? Activity        // Safe cast to Activity
    val activeNotes by viewModel.activeNotes.collectAsState()
    val todayReminders by viewModel.todayReminders.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    LaunchedEffect(authState) {
        when (authState) {
            AuthUiState.LoggedOut -> onLoginNavigate()
            AuthUiState.SwitchAccountRequired -> {
                onLoginNavigate()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AppTopBar(
                title = "My Notes",
                actions = {
                    // History button
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

                    // Logout but keep account cached
                    IconButton(onClick = { authViewModel.logoutKeepAccount() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }

                    IconButton(onClick = { activity?.let { authViewModel.switchAccount(it) } }) {
                        Icon(Icons.Default.Person, contentDescription = "Switch Account")
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
                .padding(padding),
            contentPadding = PaddingValues(8.dp)
        ) {
            // ------------------- Today's Reminders -------------------
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
                        onCompleteClick = { viewModel.markAsCompleted(note.id) },
                        onEditClick = { onEditNoteClick(note.id) },
                        onDeleteClick = { viewModel.deleteNote(note.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // ------------------- Active Notes -------------------
            item {
                Text(
                    text = "Active Notes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }

            if (activeNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("No active notes. Create one!")
                    }
                }
            } else {
                items(activeNotes) { note ->
                    NoteCard(
                        note = note,
                        onCompleteClick = { viewModel.markAsCompleted(note.id) },
                        onEditClick = { onEditNoteClick(note.id) },
                        onDeleteClick = { viewModel.deleteNote(note.id) }
                    )
                }
            }
        }
    }
}
