package com.example.takeanote1.ui.completed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.components.NoteCard
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedNotesScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit,
    onEditNoteClick: (String) -> Unit // Kept for NavGraph compatibility, but not used for NoteCard
) {
    val completedNotes by viewModel.completedNotes.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "History (Completed)",
                showBack = true,
                onBackClick = onBack
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
                        showEditButton = false, // Hide edit for completed notes
                        onDeleteClick = { viewModel.deleteNote(note.id) }
                    )
                }
            }
        }
    }
}
