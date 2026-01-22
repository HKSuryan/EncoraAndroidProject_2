package com.example.takeanote1.ui.addnote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.takeanote1.ui.home.NotesViewModel
import com.example.takeanote1.ui.components.AppTopBar
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("General") }
    var reminderTime by remember { mutableStateOf<Long?>(null) }

    val topics = listOf("General", "Work", "Personal", "Shopping", "Health")

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Add New Note",
                showBack = true,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Select Topic:", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topics.forEach { t ->
                    FilterChip(
                        selected = topic == t,
                        onClick = { topic = t },
                        label = { Text(t) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.HOUR_OF_DAY, 1)
                    reminderTime = cal.timeInMillis
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(if (reminderTime == null) "Set Today Reminder" else "Reminder Set")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        viewModel.addNote(title, content, topic, reminderTime)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save Note")
            }
        }
    }
}
