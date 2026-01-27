package com.example.takeanote1.ui.completed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedNotesScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    val completedNotes by viewModel.completedNotes.collectAsState()
    val selectedIds by viewModel.selectedCompletedNotes.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "History (Completed)",
                showBack = true,
                onBackClick = onBack,
                actions = {
                    if (selectedIds.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete All"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (completedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No completed notes yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(8.dp)
            ) {
                items(completedNotes) { note ->
                    NoteCard(
                        note = note,
                        showCompleteButton = false,
                        isSelected = selectedIds.contains(note.id),
                        onClick = {
                            viewModel.toggleCompletedNoteSelection(note.id)
                        }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Selected Notes") },
            text = {
                Text( "Are you sure you want to delete selected completed notes?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedCompletedNotes()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
